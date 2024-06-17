const path = require('path');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const CopyPlugin = require('copy-webpack-plugin');

module.exports = {
    devServer: {
        compress: true,
        onBeforeSetupMiddleware: function (server) {
            server.app.get('*.gz', function (req, res, next) {
                res.set('Content-Encoding', 'gzip');
                res.set('Content-Type', 'application/json');
                next();
            })
        }
    },
    devtool: 'source-map',
    entry: './src/Pl3xMap.ts',
    externals: {
        "leaflet": "L"
    },
    mode: 'production',
    module: {
        rules: [
            {
                test: /\.ts$/,
                use: 'ts-loader',
                include: [path.resolve(__dirname, 'src')]
            },
            {
                test: /\.(scss|css)$/,
                use: [
                    'style-loader',
                    {
                        loader: MiniCssExtractPlugin.loader,
                        options: {
                            esModule: false,
                        }
                    },
                    {
                        loader: "css-loader",
                        options: {
                            sourceMap: true,
                            url: false,
                        }
                    },
                    {
                        loader: 'sass-loader',
                        options: {
                            sassOptions: {
                                outputStyle: 'expanded',
                            }
                        }
                    }
                ],
            },
            {
                test: /\.svg$/,
                loader: 'svg-sprite-loader',
                options: {
                    symbolId: (filePath) => `icon--${path.basename(filePath, '.svg')}`,
                },
            }
        ]
    },
    output: {
        publicPath: '/',
        filename: 'pl3xmap.js',
        path: path.resolve(__dirname, 'dist')
    },
    performance: {
        maxEntrypointSize: 512000,
        maxAssetSize: 512000
    },
    resolve: {
        extensions: ['.ts', '.js', '.scss', '.css']
    },
    plugins: [
        new MiniCssExtractPlugin({
            filename: 'styles.css'
        }),
        new CopyPlugin({ patterns: [{ from: 'public' }] })
    ]
}
