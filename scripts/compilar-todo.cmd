@echo off
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0compilar-todo.ps1" %*
