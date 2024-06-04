import sys
import subprocess

def install_or_update(package):
    """
    安裝或更新指定的包
    """
    try:
        print(f"正在安裝或更新 {package} ...")
        process = subprocess.Popen(
            [sys.executable, '-m', 'pip', 'install', '--upgrade', package],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )
        stdout, stderr = process.communicate()
        print(stdout)
        if process.returncode == 0:
            print(f"{package} 安裝或更新完成")
        else:
            print(f"安裝或更新 {package} 時出錯: {stderr}")
    except subprocess.CalledProcessError as e:
        print(f"安裝或更新 {package} 時出錯: {e}")

# 更新 pip
install_or_update('pip')

# 需要檢查和安裝的套件列表
packages_to_check = [
    'selenium',
    'firebase-admin',
    'pandas',
    'yt-dlp',
    'beautifulsoup4'
]

# 檢查並安裝所需的包
for package in packages_to_check:
    try:
        __import__(package)
        print(f'{package} 已安裝')
        # 使用 install_or_update 函數來確保套件是最新的
        install_or_update(package)
    except ImportError:
        print(f'{package} 未安裝，開始安裝...')
        install_or_update(package)

print('所有套件已安裝或更新')
