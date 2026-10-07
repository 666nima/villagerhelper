from pathlib import Path
p=Path('build/createMcpToSrg/output.tsrg');out=[];owner=''
for l in p.read_text().splitlines()[1:]:
 if not l.startswith('\t'):
  a,b=l.split();owner=a;out.append(f'CL: {a} {b}')
 elif l.startswith('\t\t'):continue
 else:
  v=l.split()
  if len(v)==3 and v[1].startswith('('):out.append(f'MD: {owner}/{v[0]} {v[1]} {owner}/{v[2]} {v[1]}')
  elif len(v)==2:out.append(f'FD: {owner}/{v[0]} {owner}/{v[1]}')
Path('build/mixin.srg').write_text('\n'.join(out)+'\n')
