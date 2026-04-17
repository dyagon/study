import asyncio
import time
from fastapi import FastAPI

import threading
import logging

LOG_FORMAT = '%(asctime)s - %(processName)s - %(funcName)s - [%(levelname)s] - %(message)s'

logging.basicConfig(format=LOG_FORMAT, level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI()

# 地狱：在异步视图中使用同步 IO
@app.get("/sync-blocking")
def read_sync_blocking():
    logger.info("进入同步阻塞视图...")
    logger.info(f"当前线程: {threading.current_thread().name}, {threading.current_thread().ident}")
    time.sleep(10)  # 致命错误！整个服务器将在这里冻结 5 秒！
    logger.info("...同步阻塞视图结束")
    return {"message": "我阻塞了所有人 5 秒钟"}

# 天堂：在异步视图中使用异步 IO
@app.get("/async-non-blocking")
async def read_async_non_blocking():
    logger.info("进入异步非阻塞视图...")
    logger.info(f"当前线程: {threading.current_thread().name}, {threading.current_thread().ident}")
    start = time.time()
    await asyncio.sleep(5)  # 正确做法！服务器在这 5 秒内可以处理成千上万的其他请求
    end = time.time()
    logger.info(f"...异步非阻塞视图结束，耗时 {end - start} 秒")
    return {"message": "我等待了 5 秒，但没有阻塞任何人"}

# 灾难现场：一个 async 函数，但内部有 blocking 调用
@app.get("/async-disaster")
async def read_async_disaster():
    logger.info("进入灾难性异步视图...")
    logger.info(f"当前线程: {threading.current_thread().name}, {threading.current_thread().ident}")
    # Starlette/FastAPI 信任这个 async 函数不会阻塞。
    # 但我们在这里背叛了它的信任！
    time.sleep(5)  # 致命错误！事件循环在这里被直接冻结！
    logger.info("...灾难性视图结束")
    return {"message": "我是一个 async 函数，但我阻塞了整个服务器！"}