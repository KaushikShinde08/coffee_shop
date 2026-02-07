@echo off
echo ==========================================
echo   Build & Push Frontend and Backend
echo ==========================================

echo.
echo [1/4] Building Frontend...
cd frontend
docker build -t kaushikshinde/coffee-frontend:latest .
echo Pushing Frontend...
docker push kaushikshinde/coffee-frontend:latest
cd ..

echo.
echo [2/4] Building Backend...
docker build -t kaushikshinde/coffee-backend:latest .
echo Pushing Backend...
docker push kaushikshinde/coffee-backend:latest

echo.
echo ==========================================
echo   All images pushed to Docker Hub! 🚀
echo ==========================================
echo.
echo Frontend: kaushikshinde/coffee-frontend:latest
echo Backend:  kaushikshinde/coffee-backend:latest
pause
