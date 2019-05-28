import os

path = "D:\Google_Cloud\Anki_Korean"
i=0
for fileName in os.listdir(path):
    os.rename(os.path.join(path,fileName), os.path.join(path, 'a'+str(i)+".mp3"))
    i=i+1