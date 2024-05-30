import requests
from bs4 import BeautifulSoup
from firebase_admin import credentials, initialize_app, storage, db
import os
import time

# 初始化 Firebase Admin SDK
cred = credentials.Certificate("C:/Users/sky.ho/Desktop/Andriod_local/u10127002-movie-firebase-adminsdk-kxpxj-a1e4466877.json")
initialize_app(cred, {
    'storageBucket': 'u10127002-movie.appspot.com',
    'databaseURL': 'https://u10127002-movie-default-rtdb.firebaseio.com/'
})

# 定義抓取和上傳函數
def fetch_and_upload_movie(movie_id):
    url = f"https://www.vscinemas.com.tw/vsweb/film/detail.aspx?id={movie_id}"
    response = requests.get(url)
    soup = BeautifulSoup(response.text, 'html.parser')

    # 抓取其他資訊
    title_area = soup.find('div', class_='titleArea')
    movie_title_chinese = title_area.find('h1').text
    movie_title_english = title_area.find('h2').text
    release_date = title_area.find('time').text

    movie_type = ""
    movie_duration = ""

    type_info = soup.find_all('tr')
    for tr in type_info:
        if '類型' in tr.text:
            movie_type = tr.find_all('td')[1].text
        if '片長' in tr.text:
            movie_duration = tr.find_all('td')[1].text

    # 抓取第一段文字內容
    bbs_article = soup.find('div', class_='bbsArticle')
    first_paragraph = bbs_article.find('p').text

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

    # 刪除本地文件
    os.remove(local_image_path)

    # 打印抓取到的信息
    print(f"電影名稱（中文）：{movie_title_chinese}")
    print(f"電影名稱（英文）：{movie_title_english}")
    print(f"{release_date}")
    print(f"類型：{movie_type if movie_type else '未提供'}")
    print(f"片長：{movie_duration if movie_duration else '未提供'}")
    print(f"描述：{first_paragraph}")
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
        'Description': first_paragraph
    }
    movie_ref.set(movie_data)

    print("Image and information uploaded successfully.")
    time.sleep(2)  # 避免過多請求造成伺服器負擔


# 獲取所有電影 ID
movie_ids = [
    "7166", "7160", "7159", "7232", "7142", "7229", "7290", "7048", 
    "7300", "7301", "7152", "7208", "7099", "7271", "7266", "7233", 
    "7111", "7282", "7274", "7268", "7261", "7247", "7220", "7239", 
    "7254", "7281", "7278", "7255", "7219", "7205", "7217", "7280", 
    "7218", "7276", "7267", "7260", "7158", "7243", "7252", "7270", 
    "7238", "7148", "7146", "7211", "7147", "5807", "5068", "7154", 
    "7133", "7155", "7078", "7075", "7093", "7050"
]

# 遍歷所有電影 ID，抓取並上傳資料
for movie_id in movie_ids:
    fetch_and_upload_movie(movie_id)
