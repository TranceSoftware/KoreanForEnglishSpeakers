from google.cloud import texttospeech
import csv
import time

importName = "Correct_CSV.csv"
importFile = open(importName, 'rt', encoding='UTF-8')
reader = csv.reader(importFile)

# Instantiates a client
client = texttospeech.TextToSpeechClient()

# Set the text input to be synthesized
synthesis_input = texttospeech.types.SynthesisInput(text="생일 축하. 생일 축하.")

# Build the voice request, select the language code ("en-US") and the ssml
# voice gender ("neutral")
# voice = texttospeech.types.VoiceSelectionParams(
#     language_code='ko-KR',
#     ssml_gender=texttospeech.enums.SsmlVoiceGender.NEUTRAL)

voice2 = texttospeech.types.VoiceSelectionParams(
    language_code='ko-KR',
    name='ko-KR-Wavenet-B'
)

# Select the type of audio file you want returned
audio_config = texttospeech.types.AudioConfig(
    audio_encoding=texttospeech.enums.AudioEncoding.MP3)

# Perform the text-to-speech request on the text input with the selected
# voice parameters and audio file type
# response = client.synthesize_speech(synthesis_input, voice2, audio_config)

i = 0
for row in reader:
    if(i >= 100):
        textToSynth = row[0]
        filename = str(i) + '_' + row[0] + '.mp3'
        # filename = str(row[1]) + ".mp3"
        # textToSynth = '.' + filename
        with open(filename, 'wb+') as out:
            synthesis_input = texttospeech.types.SynthesisInput(text=textToSynth)
            response = client.synthesize_speech(synthesis_input, voice2, audio_config)
            out.write(response.audio_content)
    i=i+1
    if((i % 100) == 0):
        time.sleep(60)
    
# The response's audio_content is binary.
# with open('output_changed_birthday.mp3', 'wb') as out:
    # Write the response to the output file.
    # out.write(response.audio_content)
    # print('Audio content written to file "output_changed_birthday.mp3"')