const http = require('http');

const options = {
    host: '192.168.1.210',
    port: 8080,
    path: '/rs/lightData',
    method: 'put'
}

const putData = {
    'NumLightProbeRadials': 12,
    'NumLightProbeLightsPerRadial': 50,
    'LightProbeData': 'RkZGRkZG' // base64 encoded string
}


const req = http.request(options, (res) => {
    console.log('status:', res.statusCode)
    console.log('headers:', JSON.stringify(res.headers))
    res.setEncoding('utf8')
    res.on('data', (chunk) => {
        console.log('body:', chunk)
    })
})

req.write(JSON.stringify(putData))
req.end();

