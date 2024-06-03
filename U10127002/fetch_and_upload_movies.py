import os
import time
import glob
import requests
import firebase_admin
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import Select
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
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

# 設置 ChromeDriver 的路徑
driver = webdriver.Chrome()

# 打開網頁
url = "https://www.vscinemas.com.tw/vsweb/film/detail.aspx?id=7166"
driver.get(url)

# 等待 select 元素可見
wait = WebDriverWait(driver, 10)
select_element = wait.until(EC.presence_of_element_located((By.XPATH, "//select[@onchange]")))

# 創建 Select 物件
select = Select(select_element)

# 抓取所有選項的值
options = [(option.get_attribute("value"), option.text) for option in select.options]

# 定義抓取和上傳函數
def fetch_and_upload_movie(movie_id, movie_name):
    url = f"https://www.vscinemas.com.tw/vsweb/film/detail.aspx?id={movie_id}"
    driver.get(url)
    
    # 等待頁面加載
    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.CLASS_NAME, 'titleArea')))
    
    soup = BeautifulSoup(driver.page_source, 'html.parser')

    # 抓取其他資訊
    title_area = soup.find('div', class_='titleArea')
    movie_title_chinese = title_area.find('h1').text
    movie_title_english = title_area.find('h2').text
    release_date = title_area.find('time').text

    movie_type = "目前沒有"
    movie_duration = "目前沒有"

    type_info = soup.find_all('tr')
    for tr in type_info:
        if '類型' in tr.text:
            movie_type = tr.find_all('td')[1].text if tr.find_all('td')[1].text else "目前沒有"
        if '片長' in tr.text:
            movie_duration = tr.find_all('td')[1].text if tr.find_all('td')[1].text else "目前沒有"

    # 抓取所有段落的文字內容並處理 <br> 標籤
    bbs_article = soup.find('div', class_='bbsArticle')
    for br in bbs_article.find_all('br'):
        br.replace_with('\n')
    paragraphs = bbs_article.find_all('p')
    full_text = "\n".join([p.text for p in paragraphs]) if paragraphs else "目前沒有"

    # 提取圖片 URL
    img_tag = soup.find('img', {'alt': movie_title_chinese})
    img_url = "https://www.vscinemas.com.tw/vsweb/" + img_tag['src'][3:]  # 去掉 "../"
    image_content = requests.get(img_url).content

    # 保存圖片到本地
    local_image_path = f"{movie_title_english}.jpg"
    with open(local_image_path, "wb") as image_file:
        image_file.write(image_content)

    # 上傳圖片到 Firebase Storage
    bucket = storage.bucket()
    blob = bucket.blob(f"images/{movie_title_english}.jpg")
    blob.upload_from_filename(local_image_path)

    # 刪除本地檔
    os.remove(local_image_path)

    # 列印抓取到的資訊
    print(f"電影名稱（中文）：{movie_title_chinese}")
    print(f"電影名稱（英文）：{movie_title_english}")
    print(f"{release_date}")
    print(f"類型：{movie_type if movie_type else '目前沒有'}")
    print(f"片長：{movie_duration if movie_duration else '目前沒有'}")
    print(f"描述：{full_text}")
    print(f"圖片網址：{img_url}")
    print(f"Uploading to: images/{movie_title_english}.jpg")

    # 上傳資料到 Firebase Realtime Database
    ref = db.reference('Movie')
    movie_ref = ref.child(movie_title_english)
    movie_data = {
        'Title(Chinese)': movie_title_chinese,
        'Title(English)': movie_title_english,
        'Release Date': release_date,
        'Type': movie_type,
        'Duration': movie_duration,
        'Description': full_text
    }
    movie_ref.set(movie_data)

    print("Image and information uploaded successfully.")
    time.sleep(2)  # 避免過多請求造成伺服器負擔

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

            # 選擇當前電影
            select.select_by_value(option_value)

            # 列印當前電影名稱
            print(f"電影：{option_text}")

            # 等待3秒以確保頁面加載完成
            time.sleep(3)

            # 抓取並上傳資料
            fetch_and_upload_movie(option_value, option_text)

            # 等待 select 元素可見
            wait = WebDriverWait(driver, 10)
            select_element = wait.until(EC.presence_of_element_located((By.XPATH, "//select[@onchange]")))
            select = Select(select_element)

finally:
    # 關閉瀏覽器
    driver.quit()
