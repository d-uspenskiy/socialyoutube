async function postData(url = '', data = {}) {
  // Default options are marked with *
  const response = await fetch(url, {
    method: 'POST',
    cache: 'no-cache',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
  });
  if (!response.ok) {
    const text = await response.text();
    console.log("Bad response", response.status, text);
    throw new Error(`Bad response ${response.status}`);
  }
  return response.json();
}
