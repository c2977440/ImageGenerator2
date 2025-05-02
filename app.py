from flask import Flask, jsonify, request
import logging
from logging.handlers import RotatingFileHandler
import os

# 初始化 Flask 應用程式
app = Flask(__name__)

# 設定日誌
if not os.path.exists('logs'):
    os.mkdir('logs')
file_handler = RotatingFileHandler('logs/app.log', maxBytes=10240, backupCount=10)
file_handler.setFormatter(logging.Formatter(
    '%(asctime)s %(levelname)s: %(message)s [in %(pathname)s:%(lineno)d]'
))
file_handler.setLevel(logging.INFO)
app.logger.addHandler(file_handler)
app.logger.setLevel(logging.INFO)
app.logger.info('應用程式啟動')

# 錯誤處理
class APIError(Exception):
    def __init__(self, message, status_code=400, payload=None):
        super().__init__()
        self.message = message
        self.status_code = status_code
        self.payload = payload

    def to_dict(self):
        rv = dict(self.payload or ())
        rv['message'] = self.message
        rv['status'] = 'error'
        return rv

@app.errorhandler(APIError)
def handle_api_error(error):
    response = jsonify(error.to_dict())
    response.status_code = error.status_code
    return response

@app.errorhandler(404)
def not_found_error(error):
    return jsonify({
        'status': 'error',
        'message': '找不到請求的資源'
    }), 404

@app.errorhandler(500)
def internal_error(error):
    app.logger.error(f'伺服器錯誤: {error}')
    return jsonify({
        'status': 'error',
        'message': '伺服器內部錯誤'
    }), 500

# 健康檢查端點
@app.route('/health')
def health_check():
    return jsonify({
        'status': 'success',
        'message': '服務正常運行中'
    })

# 主要路由
@app.route('/')
def index():
    return jsonify({
        'status': 'success',
        'message': '歡迎使用 API 服務'
    })

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000) 