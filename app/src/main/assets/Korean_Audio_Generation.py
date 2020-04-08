from google.cloud import texttospeech
import csv
import time
import six

client = texttospeech.TextToSpeechClient()

importName = "My_Korean_1600.csv"
importFile = open(importName, 'rt', encoding='utf-8')
# importFile = open(importName, 'rt', encoding='ISO-8859-1')
reader = csv.reader(importFile)

count = 1600
for row in reader:
    delim_row = row[0].split("\t")
    # print(delim_row[1] + "-" + str(count))
    if(count<100000):
        text = delim_row[1]
        # print(str(row[1]))
        output = 'a_ko_' + delim_row[1] + '.mp3' 
        synthesis_input = texttospeech.types.SynthesisInput(text=delim_row[1])

        voice = texttospeech.types.VoiceSelectionParams(
            language_code='ko',
            name='ko-KR-Wavenet-A')
            # ssml_gender=texttospeech.enums.SsmlVoiceGender.FEMALE)
        # audio_config = texttospeech.types.AudioConfig(
        #     audio_encoding=texttospeech.enums.AudioEncoding.MP3,
        #     effects_profile_id=[effects_profile_id])
        audio_config = texttospeech.types.AudioConfig(
            audio_encoding=texttospeech.enums.AudioEncoding.MP3)
        response = client.synthesize_speech(synthesis_input, voice, audio_config)
        with open(output, 'wb') as out:
            out.write(response.audio_content)
            print('Audio content written to file "%s"' % output)
    count = count + 1
    if(count % 20 == 0):
        time.sleep(60)
        print("Sleeping: count - " + str(count))