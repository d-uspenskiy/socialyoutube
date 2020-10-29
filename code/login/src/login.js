function login(state) {
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
  var params = {'client_id': '404207598018-fn1h0bl6298ms1ks7rc9988f97s6luti.apps.googleusercontent.com',
                'redirect_uri': redirect.join('/'),
                'response_type': 'id_token',
                'scope': 'email',
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

var url = window.location;
console.log(url);
var search = url.search.substr(1);
console.log(search);
var sp = new URLSearchParams(search);
console.log(sp)
for (var p of sp) {
  console.log(p[0], p[1]);
}

login(sp.get('state'));
