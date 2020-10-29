const webpack = require('webpack');
const path = require('path');
const ROOT = __dirname;
const DESTINATION = path.join(ROOT, '/dist');

const VueLoaderPlugin = require('vue-loader/lib/plugin')

/** wepback resolve */
const RESOLVE = {
  alias: {
    'vue$': 'vue/dist/vue.esm.js'
  },
  extensions: ['.tsx', '.ts', '.js']
};

/** webpack plugins */
const PLUGINS = [ new VueLoaderPlugin() ];
const MODULE = {
  rules: [
    // Scripts
    {
      test: /\.tsx?$/,
      exclude: [/node_modules/],
      loader: 'ts-loader',
      options: {
        appendTsSuffixTo: [/\.vue$/]
      }
    },
    {
      test: /\.vue$/,
      loader: 'vue-loader',
      options: {
        loaders: {
            ts: 'ts-loader'
        },
        esModule: true
      }
    },
    {
      test: /\.css/,
      loaders: ['style-loader', 'css-loader']
    }
  ]
};
const OUTPUT = {
  filename: 'index.js',
  path: DESTINATION,
};

module.exports = {
  node: {
    fs: 'empty',
  },
  entry: {
    app: path.resolve(__dirname, './src/index.ts')
  },
  context: ROOT,
  resolve: RESOLVE,
  mode: 'development',
  module: MODULE,
  plugins: PLUGINS,
  devServer: {},
  output: OUTPUT,
};
