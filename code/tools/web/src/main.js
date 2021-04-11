function loginImpl(state) {
  console.log("LOGIN");
  var oauth2Endpoint = 'https://accounts.google.com/o/oauth2/v2/auth';

  // Create <form> element to submit parameters to OAuth 2.0 endpoint.
  var form = document.createElement('form');
  form.setAttribute('method', 'GET'); // Send as a GET request.
  form.setAttribute('action', oauth2Endpoint);

  var redirect = window.location.href.split('/');
  redirect[redirect.length - 1] = 'response.html';
  console.log("REDIRECT", redirect);
  // Parameters to pass to OAuth 2.0 endpoint.
  var params = {'client_id': '849022792907-450tk630g7tu49agclflrjbndd0mutfn.apps.googleusercontent.com',
                'redirect_uri': redirect.join('/'),
                'response_type': 'id_token',
                'scope': 'email profile',
                'nonce': Math.random().toString(36),
                'state': state};

  // Add form parameters as hidden input values.
  for (var p in params) {
    var input = document.createElement('input');
    input.setAttribute('type', 'hidden');
    input.setAttribute('name', p);
    input.setAttribute('value', params[p]);
    form.appendChild(input);
  }

  // Add form to page and submit it to open the OAuth 2.0 endpoint.
  document.body.appendChild(form);
  form.submit();
}

/*var url = window.location;
console.log(url);
var search = url.search.substr(1);
console.log(search);
var sp = new URLSearchParams(search);
console.log(sp)
for (var p of sp) {
  console.log(p[0], p[1]);
}

login(sp.get('state'));*/

class Button {
  constructor(name, handler) {
    this.name = name;
    this.handler = handler;
  }
}

class Core {
  constructor() {
    console.log("CORE");
    var container = byId("buttons");
    this.token_ = window.localStorage.getItem("token");
    console.log("Current token ", this.token_);
    this.rest_ = new REST(undefined, false, function() {return this.token_ && ("Bearer " + this.token_) }.bind(this));
    var items = [
      new Button("Login", this.login.bind(this)), 
      new Button("Logout All", function() { requestJSON(this.rest_, "POST", "/backend/auth/logout_all");}.bind(this)),
      new Button("Renew", this.renewToken.bind(this)),
      new Button("Self", function() { requestJSON(this.rest_, "GET", "/backend/self");}.bind(this))];
    for (var i of items) {
      var b = appendNewNode(container, "button");
      b.type = "button";
      b.innerText = i.name;
      b.onclick = i.handler;
      appendNewNode(container, "br");
    }
  }

  login() {
    loginImpl("test");
  }

  renewToken() {
    postData('/backend/auth/renew', { token: this.token_ })
    .then(data => {
      window.localStorage.setItem("token", data.token);
      this.handleNewToken(data);
    });
  }

  handleNewToken(rsp) {
    console.log("New token", rsp);
    this.token_ = rsp.token;
  }
}

var core = null;

document.addEventListener('DOMContentLoaded', function() {core = new Core();});
