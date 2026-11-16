import wave,math,struct,random
from pathlib import Path
# Original synthesized one-shot instruments. No recordings or third-party audio.
for name,freq,duration in [('aether_mark',440,.24),('aether_bell',196,.65),('aether_iron',320,.26),('aether_wood',110,.25),('aether_page',850,.16)]:
 rng=random.Random(145);rate=22050;frames=[]
 for n in range(int(duration*rate)):
  t=n/rate;env=min(1,t/.006)*math.exp(-t*7/duration)
  tone=(math.sin(2*math.pi*freq*t)+.3*math.sin(2*math.pi*freq*2.71*t)+.12*math.sin(2*math.pi*freq*4.13*t))
  if name=='aether_page':tone=rng.uniform(-1,1)*.5
  if name=='aether_wood':tone=tone*.7+rng.uniform(-1,1)*math.exp(-t*80)*.4
  frames.append(struct.pack('<h',int(max(-1,min(1,tone*.25*env))*32767)))
 with wave.open(str(Path('app/src/main/res/raw')/(name+'.wav')),'wb') as w:w.setnchannels(1);w.setsampwidth(2);w.setframerate(rate);w.writeframes(b''.join(frames))
