import re
import os
import glob
import time
import firebase_admin
from firebase_admin import credentials, db
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.support.ui import Select
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from webdriver_manager.chrome import ChromeDriverManager

# 從設定檔中讀取 Firebase URL
import config

# 獲取當前檔案所在的目錄
current_directory = os.path.dirname(os.path.abspath(__file__))

# 查找當前目錄中的所有 JSON 憑證檔
json_files = glob.glob(os.path.join(current_directory, "*.json"))

if not json_files:
    raise FileNotFoundError("沒有找到任何 JSON 憑證文件")

# 使用第一個找到的 JSON 憑證檔
cred = credentials.Certificate(json_files[0])

# 初始化 Firebase 應用
firebase_admin.initialize_app(cred, {
    'databaseURL': config.FIREBASE_URL
})

def sanitize_key(key):
    return re.sub(r'[.$#\[\]/]', '-', key)

# 設置 Chrome 瀏覽器
options = webdriver.ChromeOptions()
# options.add_argument('--headless')  # 無頭模式
driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=options)

try:
    # 打開目標網頁
    url = "https://www.vscinemas.com.tw/ShowTimes/"
    driver.get(url)

    # 等待第一個下拉選單加載
    wait = WebDriverWait(driver, 10)
    select_element_f = wait.until(EC.presence_of_element_located((By.ID, "CinemaNameTWInfoF")))

    # 選擇臺北信義威秀影城 (TP)
    select_f = Select(select_element_f)
    select_f.select_by_value("TP")

    # 等待2秒以確保內容加載完成
    time.sleep(2)

    # 等待第二個下拉選單加載
    select_element_s = wait.until(EC.presence_of_element_located((By.ID, "CinemaNameTWInfoS")))

    # 抓取所有選項的值
    select_s = Select(select_element_s)
    options = [(option.get_attribute("value"), option.text) for option in select_s.options]

    # 遍歷每個選項
    show_times = {}

    for option_value, option_text in options:
        if option_value:  # 跳過第一個 "請選擇查詢影城" 選項
            # 選擇當前影城
            select_s.select_by_value(option_value)

            # 列印當前影城名稱
            print(f"影城：{option_text}")

            # 等待2秒以確保內容加載完成
            time.sleep(2)

            # 抓取所有 class="col-xs-12 LangTW MovieName" 的元素
            movie_name_tags = driver.find_elements(By.CSS_SELECTOR, 'strong.col-xs-12.LangTW.MovieName')

            cinema_info = {}

            # 遍歷每個電影名稱
            for tag in movie_name_tags:
                movie_name = tag.text.strip()
                print(f"電影名稱：{movie_name}")

                # 抓取與這個電影名稱相鄰的日期和時間資訊
                parent = tag.find_element(By.XPATH, '..')
                show_dates = parent.find_elements(By.CSS_SELECTOR, 'strong.col-xs-12.LangTW.RealShowDate')
                session_times = parent.find_elements(By.CSS_SELECTOR, 'div.col-xs-12.SessionTimeInfo')

                movie_info = {}
                for date, times in zip(show_dates, session_times):
                    show_date = date.text.strip()
                    print(f"{show_date}")
                    time_slots = [time_div.text.strip() for time_div in times.find_elements(By.CLASS_NAME, 'col-xs-0')]

                    day_info = {}
                    for time_slot in time_slots:
                        print(f"時間：{time_slot}")
                        day_info[sanitize_key(time_slot)] = time_slot

                    movie_info[sanitize_key(show_date)] = day_info

                cinema_info[sanitize_key(movie_name)] = movie_info

            show_times[sanitize_key(option_text)] = cinema_info

    # 上傳資料到 Firebase
    ref = db.reference('ShowTimes')
    ref.set(show_times)

    print("資料已成功上傳到 Firebase")

finally:
    # 關閉瀏覽器
    driver.quit()


