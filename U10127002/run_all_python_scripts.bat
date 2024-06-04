@echo off

echo Install Python Necessary package
python install_packages.py
if %errorlevel% neq 0 (
    echo Error running install_packages.py
    pause
    exit /b %errorlevel%
)


echo Running fetch_and_upload_movies.py
python fetch_and_upload_movies.py
if %errorlevel% neq 0 (
    echo Error running fetch_and_upload_movies.py
    pause
    exit /b %errorlevel%
)

echo Running fetch_and_upload_movies_with_videos.py
python fetch_and_upload_movies_with_videos.py
if %errorlevel% neq 0 (
    echo Error running fetch_and_upload_movies_with_videos.py
    pause
    exit /b %errorlevel%
)

echo Running fetch_showtimes_and_upload.py
python fetch_showtimes_and_upload.py
if %errorlevel% neq 0 (
    echo Error running fetch_showtimes_and_upload.py
    pause
    exit /b %errorlevel%
)

echo All scripts ran successfully
pause
