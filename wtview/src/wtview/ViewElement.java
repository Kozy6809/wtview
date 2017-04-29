package wtview;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;
import java.awt.*;
/**
   表示したい画像の情報を保持するオブジェクト
*/
class ViewElement implements Serializable {
	/**
	 * <code>serialVersionUID</code> のコメント
	 */
	private static final long serialVersionUID = 1L;
	
    int wt = 0; // 重み
    String name; // フルパス名
    double mag = 1.0; // 表示倍率
    int refs = 0; // 参照回数
    long ts =0; // 最後に表示された時のタイムスタンプ
    transient volatile Image img;
    transient volatile Image simg; // スケーリングされた画像
    transient long laps = 0; // 積算表示時間
    transient long time0; // 計時の起点
    transient boolean timeOn = false; // 計時中
    transient int up = 0; // ランクアップリクエスト回数
    transient int down = 0; // ランクダウンリクエスト回数
    transient boolean extra = false; // エクストラリング指定

    void startDspTime() {
    	if (timeOn) return;
    	time0 = System.currentTimeMillis();
    	timeOn = true;
    }
    void stopDspTime() {
    	if (!timeOn) return;
    	laps += (System.currentTimeMillis() - time0);
    	timeOn = false;
    }
    
    /**
     * 別のViewElementに切り替える際の表示終了手続き
     *
     */
    void changeVe() {
    	stopDspTime();
    	ts = System.currentTimeMillis();
    }
    
    void flush() {
    	// リソース解放
    	if (img != null) img.flush();
    	if (simg != null) simg.flush();
    	img = null;
    	simg = null;
    	if (Runtime.getRuntime().freeMemory() < 10000000L) System.gc();
    }
}
