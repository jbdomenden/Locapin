const api={
  async request(url,opts={}){const res=await fetch(url,{headers:{'Content-Type':'application/json'},credentials:'include',...opts});if(res.status===401){location.href='/admin/login';return}const j=await res.json().catch(()=>({}));if(!res.ok||j.success===false)throw new Error(j.message||'Request failed');return j.data;},
  get:(u)=>api.request(u),post:(u,d)=>api.request(u,{method:'POST',body:JSON.stringify(d)}),put:(u,d)=>api.request(u,{method:'PUT',body:JSON.stringify(d)}),patch:(u,d)=>api.request(u,{method:'PATCH',body:JSON.stringify(d)}),del:(u)=>api.request(u,{method:'DELETE'})
};
