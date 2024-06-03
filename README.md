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
1. 生成 Firebase 的 Key。
2. 將生成的 Key 放置在 Python 腳本同一個資料夾內。
3. 將 Key 放置在與 `settings.gradle.kts` 同一層目錄 (`/test_movie/`) 下。

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
pip install selenium firebase-admin pandas yt-dlp beautifulsoup4
```

#### Python 腳本執行
- 點擊兩下 `run_all_python_scripts.bat` 來執行所有 Python 腳本，將資料收集到 Firebase 中。
