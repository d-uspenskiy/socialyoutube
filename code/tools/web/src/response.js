var url = window.location;
console.log(url);
var search = url.hash.substr(1);
console.log(search);
var sp = new URLSearchParams(search);
console.log(sp)
for (var p of sp) {
  console.log(p[0], p[1]);
}

postData('/backend/auth/login', { token: sp.get("id_token") })
  .then(data => {
    window.localStorage.setItem("token", data.token);
    console.log(data);
    var loc = window.location;
    var target = `${loc.protocol}//${loc.host}`;
    console.log("Redirecting to ", target);
    window.location.href = target;
  });
