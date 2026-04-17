import timeit

# 场景 1: 全局函数 (基准)
setup_global = """
def process(x):
    return x * 2
"""
test_global = """
for i in range(1000):
    process(i)
"""

# 场景 2: 内嵌函数，但只创建一次，多次调用
# 这主要测试闭包变量的【调用开销】
setup_nested_call_only = """
def factory():
    n = 2
    def inner_process(x):
        return x * n # 访问闭包变量 n
    return inner_process

p = factory()
"""
test_nested_call_only = """
for i in range(1000):
    p(i) # p 是已经创建好的内嵌函数
"""

# 场景 3: 每次循环都重新创建并调用内嵌函数
# 这同时测试了【创建开销】和【调用开销】
setup_nested_recreate = """
def factory():
    n = 2
    def inner_process(x):
        return x * n # 访问闭包变量 n
    return inner_process

"""
test_nested_recreate = """
for i in range(1000):
    factory()(i)
"""

# --- 执行测试 ---
t1 = timeit.timeit(stmt=test_global, setup=setup_global, number=10000)
t2 = timeit.timeit(stmt=test_nested_call_only, setup=setup_nested_call_only, number=10000)
t3 = timeit.timeit(stmt=test_nested_recreate, setup=setup_nested_recreate, number=10000)

print(f"全局函数:        {t1:.6f} 秒")
print(f"内嵌函数 (仅调用): {t2:.6f} 秒 (比全局函数慢了 {t2/t1:.2f} 倍)")
print(f"内嵌函数 (重创建): {t3:.6f} 秒 (比全局函数慢了 {t3/t1:.2f} 倍)")
