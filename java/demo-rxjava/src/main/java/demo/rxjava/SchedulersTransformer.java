package demo.rxjava;

import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SchedulersTransformer {

    private SchedulersTransformer() {
        // Prevent instantiation
    }

    public static <T> ObservableTransformer<T, T> applySchedulers() {
        return upstream -> {
            // upstream 就是调用 .compose() 之前的 Observable
            // 在这里应用你想要的操作符链
            return upstream
                    // .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation()); // 假设在计算线程处理结果
        };
    }
}
