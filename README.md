### 更新後的 Readme

### 電影預購票卷APP

#### 作者
U10127002 何國琳

#### 開發環境
- **Android Studio 版本:** 
  - Android Studio Hedgehog | 2023.1.1 Patch 1
  - Build #AI-231.9392.1.2311.11255304, built on December 27, 2023
  - Runtime version: 17.0.7+0-b2043.56-10550314 amd64
  - VM: OpenJDK 64-Bit Server VM by JetBrains s.r.o.
  - OS: Windows 10.0
  - GC: G1 Young Generation, G1 Old Generation
  - Memory: 2048M
  - Cores: 20
  - Registry:
    - `external.system.auto.import.disabled=true`
    - `debugger.new.tool.window.layout=true`
    - `ide.text.editor.with.preview.show.floating.toolbar=false`
    - `ide.experimental.ui=true`

#### 依賴項目
```groovy
dependencies {
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

#### Firebase 配置
1. 將生成的所有 Firebase JSON 憑證文件放置在與 Python 腳本同一目錄中。
2. 創建 `config.py` 文件並設置 `FIREBASE_URL` 和 `FIREBASE_STORAGE_BUCKET` 變量：

   ```python
   # config.py
   FIREBASE_URL = 'https://u10127002-movie-default-rtdb.firebaseio.com/'
   FIREBASE_STORAGE_BUCKET = 'u10127002-movie.appspot.com'
   ```

#### Python 開發環境
- **Python 版本:** 
  ```bash
  python --version
  Python 3.12.1
  ```

- **pip 版本:**
  ```bash
  pip3 --version
  pip 24.0 from ~\AppData\Roaming\Python\Python312\site-packages\pip (python 3.12)
  ```

#### 需要安裝的 Python 包
請使用以下命令安裝所需的 Python 包：
```bash
pip install selenium firebase-admin pandas yt-dlp beautifulsoup4 webdriver-manager
```

#### Python 腳本執行
1. 確保 `config.py` 文件存在並正確設置 `FIREBASE_URL` 和 `FIREBASE_STORAGE_BUCKET` 變量，為自己的 FIREBASE STORAGE,FIREBASE Realtime 的連結
2. 點擊兩下 `run_all_python_scripts.bat` 來執行所有 Python 腳本，將資料收集到 Firebase 中。

`FIREBASE_STORAGE_BUCKET` 是用來配置 Firebase Storage 的參數。這個參數的值應該是你的 Firebase 項目的存儲桶（bucket）URL。

如果你還不確定你的 Firebase Storage Bucket URL，可以按照以下步驟找到它：

### 查找 Firebase Storage Bucket URL

1. **登錄 Firebase 控制台**：打開 [Firebase 控制台](https://console.firebase.google.com/) 並登錄到你的帳戶。
2. **選擇你的項目**：選擇你創建的項目 eg:`u10127002-movie`。
3. **導航到 Storage**：在左側導航欄中，點擊「Storage」。
4. **查看 Bucket URL**：在 Storage 界面，你可以看到你的 Storage Bucket 的 URL。通常它的格式為 `<project-id>.appspot.com`，例如 `u10127002-movie.appspot.com`。

### 確認 `FIREBASE_STORAGE_BUCKET`

一旦你確認了你的 Storage Bucket URL，就可以在 `config.py` 文件中正確設置它。

### 更新的 `config.py` 文件

```python
# config.py

FIREBASE_URL = 'https://u10127002-movie-default-rtdb.firebaseio.com/'
FIREBASE_STORAGE_BUCKET = 'u10127002-movie.appspot.com'
```
