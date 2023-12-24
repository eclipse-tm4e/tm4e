@echo off
echo Batch File Example

REM Variables
set variable1=Value1
set variable2=Value2

REM Echo variables
echo Variable 1: %variable1%
echo Variable 2: %variable2%
echo.

REM Conditional statement
set /p choice=Do you want to continue? (y/n):
if /i "%choice%"=="y" (
  echo You chose to continue.
) else (
  echo Exiting script.
  goto :eof
)

REM Loop
echo.
echo Counting to 3:
for /l %%i in (1, 1, 3) do (
  echo %%i
)

REM User input
set /p userinput=Enter something:
echo You entered: %userinput%

REM File operations
echo.
echo Creating a text file...
echo Some text > example.txt

REM Displaying file content
echo.
type example.txt

REM Deleting the file
del example.txt

REM End of script
echo.
echo Batch script completed.
