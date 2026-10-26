import csv
from pathlib import Path
import matplotlib.pyplot as plt
rows=list(csv.DictReader(open('results/benchmark.csv',encoding='utf-8')))
Path('plots').mkdir(exist_ok=True)
def plot(names,scenario,path,xlabel):
    fig,ax=plt.subplots(figsize=(8,5))
    for name in names:
        data=sorted((int(r['input_size']),float(r['avg_ms'])) for r in rows if r['algorithm']==name and r['scenario']==scenario)
        ax.plot([a for a,b in data],[b for a,b in data],marker='o',label=name)
    ax.set(xlabel=xlabel,ylabel='Average time (ms)');ax.legend();ax.grid(alpha=.3);fig.tight_layout();fig.savefig(path,dpi=170);plt.close(fig)
plot(['LCS','EditDistance'],'random','plots/dynamic_programming.png','String length (each input)')
plot(['Naive','KMP','RabinKarp'],'present','plots/string_matching_present.png','Text length')
plot(['Naive','KMP','RabinKarp'],'absent','plots/string_matching_absent.png','Text length')
plot(['Kasai'],'random','plots/kasai.png','String length')
