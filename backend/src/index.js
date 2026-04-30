const app = require('./app');

const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
    console.log(`Backend running on port ${PORT}`);
    if (process.env.DEV_MODE === 'true') {
        console.log('[DEV MODE] SMS verification codes will be printed to console');
    }
});
