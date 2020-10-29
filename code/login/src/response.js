var url = window.location;
console.log(url);
var search = url.hash.substr(1);
console.log(search);
var sp = new URLSearchParams(search);
console.log(sp)
for (var p of sp) {
  console.log(p[0], p[1]);
}
var st = sp.get('state');
if (st) {
  var parts = st.split('|');
  if (parts.length == 2 && parts[0] == 'redirect') {
    window.location = parts[1] + sp.get('id_token');
  } 
}