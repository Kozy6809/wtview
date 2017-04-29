package wtview;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

/**
 * 表示すべき画像を決定する。画像の切り替えリクエストが来た際に、現在の画像の表示時間から重みを再計算する。
 */
class ShowStrategy {
	private List<SessionContext> scs = new ArrayList<SessionContext>();
	private SessionContext sc;

	/**
	 * @param name 重みデータベースのファイル名
	 * @param sv 画像を表示するSimpleView
	 */
	ShowStrategy(String[] name, SimpleView sv, Frame window) {
		int i = 0;
		for (String dbFile : name) {
			WtContainer t = WtContainer.read(dbFile);
			if (t == null) continue;
			scs.add(new SessionContext(i, dbFile, t, sv, window));
			i++;
		}
		sc = scs.get(0);
		sc.start();
	}

	/**
	 * セッションを切り替える
	 * 
	 * @param セッション番号。値が-1ならエキストラモードのトグル、-2ならゼロモードのトグル
	 */
	void changeSession(int i) {
		if (i == -1) {
			sc.toggleExtraMode();
			return;
		}
		if (i == -2) {
			sc.toggleZeroMode();
			return;
		}

		if (i >= scs.size()) return;
		sc.stop();
		sc = scs.get(i);
		sc.start();
	}

	/**
	 * 次の画像を表示する
	 */
	void next() {
		sc.next();
	}

	/**
	 * 画像がエキストラ指定された時の処理
	 */
	void extraSpecified() {
		sc.extraSpecified();
	}
	
	/**
	 * 計時を中断する
	 */
	void pause() {
		sc.pause();
	}
	
	/**
	 * 計時を再開する
	 */
	void breakPause() {
		sc.breakPause();
	}

	/**
	 * 終了処理。重みデータベースをファイルに出力する
	 */
	int terminate() {
		int sum = 0;
		for (SessionContext sc : scs) {
			sum += sc.terminate();
		}
		return sum;
	}
}
