import os
import re
import time
import glob
import yt_dlp
import pandas as pd
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import Select
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import firebase_admin
from firebase_admin import credentials, initialize_app, storage, db
import config  # Import config file


# 獲取當前檔案所在的目錄
current_directory = os.path.dirname(os.path.abspath(__file__))

# 查找當前目錄中的所有 JSON 憑證檔
json_files = glob.glob(os.path.join(current_directory, "*.json"))

if not json_files:
    raise FileNotFoundError("沒有找到任何 JSON 憑證文件")

# 使用第一個找到的 JSON 憑證檔
cred = credentials.Certificate(json_files[0])

# 從設定檔中讀取 Firebase 配置
firebase_url = config.FIREBASE_URL
firebase_storage_bucket = config.FIREBASE_STORAGE_BUCKET

# 調試信息
print(f"FIREBASE_URL: {firebase_url}")
print(f"FIREBASE_STORAGE_BUCKET: {firebase_storage_bucket}")

if not firebase_url:
    raise ValueError("設定檔 'config.py' 中的 'FIREBASE_URL' 未設置")

if not firebase_storage_bucket:
    raise ValueError("設定檔 'config.py' 中的 'FIREBASE_STORAGE_BUCKET' 未設置")

# 初始化 Firebase 應用
firebase_admin.initialize_app(cred, {
    'databaseURL': firebase_url,
    'storageBucket': firebase_storage_bucket
})

# 設置 EdgeDriver 的路徑
driver = webdriver.Chrome()

# 打開網頁
url = "https://www.vscinemas.com.tw/vsweb/film/detail.aspx?id=7166"
driver.get(url)

# 創建 Excel 文件
df = pd.DataFrame(columns=["電影名稱", "影片連結"])

def download_and_upload_video(movie_name, video_url):
    # 使用規則運算式替換非法字元
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

        # 刪除本地影片檔
        os.remove(f'{safe_movie_name}.mp4')

        print(f"影片上傳到: {blob.public_url}")

        return blob.public_url
    except Exception as e:
        print(f"下載或上傳影片失敗 for {movie_name}: {e}")
        return None

def sanitize_movie_name(movie_name):
    # 使用規則運算式替換非法字元
    return re.sub(r'[.$\[\]#\/]', '_', movie_name)

def save_to_firebase(movie_name, video_url):
    # 清理電影名稱以去掉非法字元
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

            # 列印當前電影名稱
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

                # 保存無影片提供資訊到 Firebase Realtime Database
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


