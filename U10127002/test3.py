import os
import time
import pandas as pd
import re
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import Select
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import yt_dlp
from firebase_admin import credentials, initialize_app, storage, db

# 初始化 Firebase Admin SDK
cred = credentials.Certificate("u10127002-movie-firebase-adminsdk-kxpxj-a1e4466877.json")
initialize_app(cred, {
    'storageBucket': 'u10127002-movie.appspot.com',
    'databaseURL': 'https://u10127002-movie-default-rtdb.firebaseio.com/'
})

# 設置 EdgeDriver 的路徑
driver = webdriver.Edge()

# 打開網頁
url = "https://www.vscinemas.com.tw/vsweb/film/detail.aspx?id=7166"
driver.get(url)

# 創建 Excel 文件
df = pd.DataFrame(columns=["電影名稱", "影片連結"])

def download_and_upload_video(movie_name, video_url):
    # 使用正则表达式替换非法字符
    safe_movie_name = re.sub(r'[\\/*?:"<>|]', '_', movie_name)

    # 設定 yt-dlp 下載選項
    ydl_opts = {
        'format': 'best',
        'outtmpl': f'{safe_movie_name}.mp4',
        'noprogress': True,
        'quiet': True,
    }

    try:
        # 下載影片
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            ydl.download([video_url])

        # 上傳影片到 Firebase Storage
        bucket = storage.bucket()
        blob = bucket.blob(f'videos/{safe_movie_name}.mp4')
        blob.upload_from_filename(f'{safe_movie_name}.mp4')

        # 刪除本地影片文件
        os.remove(f'{safe_movie_name}.mp4')

        print(f"影片上傳到: {blob.public_url}")

        return blob.public_url
    except Exception as e:
        print(f"下載或上傳影片失敗 for {movie_name}: {e}")
        return None

def sanitize_movie_name(movie_name):
    # 使用正则表达式替换非法字符
    return re.sub(r'[.$\[\]#\/]', '_', movie_name)

def save_to_firebase(movie_name, video_url):
    # 清理電影名稱以去掉非法字符
    safe_movie_name = sanitize_movie_name(movie_name)
    ref = db.reference('MP4')
    ref.child(safe_movie_name).set({'連結': video_url})
    print(f"已保存 {movie_name} 的連結到 Firebase Realtime Database")

try:
    # 等待 select 元素可見
    wait = WebDriverWait(driver, 10)
    select_element = wait.until(EC.presence_of_element_located((By.XPATH, "//select[@onchange]")))

    # 創建 Select 物件
    select = Select(select_element)

    # 抓取所有選項的值
    options = [(option.get_attribute("value"), option.text) for option in select.options]

    # 遍歷每個選項
    for option_value, option_text in options:
        if option_value:  # 跳過第一個 "瀏覽其他電影" 選項
            # 跳過指定的電影選項
            #if option_text in [
                #"G4-2024 NBA Finals現場直播",
                #"G3-2024 NBA Finals現場直播",
                #"G2-2024 NBA Finals現場直播",
                #"G1-2024 NBA Finals現場直播"
            #]:
                #print(f"跳過電影：{option_text}")
                #continue

            # 選擇當前電影
            select.select_by_value(option_value)

            # 打印當前電影名稱
            print(f"電影：{option_text}")

            # 等待3秒以確保頁面加載完成
            time.sleep(3)

            # 查找 iframe 元素並獲取其 src 屬性
            try:
                iframe = wait.until(EC.presence_of_element_located((By.XPATH, "//iframe[@src]")))
                iframe_src = iframe.get_attribute("src")
                print(f"iframe src: {iframe_src}")

                # 記錄電影名稱和影片連結
                new_row = pd.DataFrame({"電影名稱": [option_text], "影片連結": [iframe_src]})
                df = pd.concat([df, new_row], ignore_index=True)

                # 下載並上傳影片
                public_url = download_and_upload_video(option_text, iframe_src)

                # 保存抓取到的影片連結到 Firebase Realtime Database
                save_to_firebase(option_text, iframe_src)
            except Exception as e:
                print(f"找不到 iframe 元素 for {option_text}: {e}")
                # 記錄電影名稱和無影片提供
                new_row = pd.DataFrame({"電影名稱": [option_text], "影片連結": ["無提供影片"]})
                df = pd.concat([df, new_row], ignore_index=True)

                # 保存無影片提供信息到 Firebase Realtime Database
                save_to_firebase(option_text, "無提供影片")

            # 模擬返回原來的網頁（重新加載原始網頁）
            driver.get(url)
            select_element = wait.until(EC.presence_of_element_located((By.XPATH, "//select[@onchange]")))
            select = Select(select_element)

finally:
    # 關閉瀏覽器
    driver.quit()

    # 將結果保存到 Excel
    df.to_excel("movies_and_videos.xlsx", index=False)
    print("結果已保存到 movies_and_videos.xlsx")
