/*
 * 作成日: 2007/05/29
 *
 * TODO この生成されたファイルのテンプレートを変更するには次へジャンプ:
 * ウィンドウ - 設定 - Java - コード・スタイル - コード・テンプレート
 */
package wtview;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import java.awt.Point;
/**
 * セッションを管理する。セッションは一つの重みデータベースに基づいた画像表示シーケンスを構成する<p>
 * 重みデータベースからの画像取り出しアルゴリズムとして、以下の要件を立てる。<p>
 * 1.重みの大きい画像ほど取り出し確率が高くなる
 * 2.重みが同程度なら参照回数の小さいものを優先する->以前の表示時刻が最も古いものに変更
 * 3.表示は重みの大きいものから開始し、順次低いものに移る。一巡したらまた重みの大きいものが取り出される
 * これらの要件を満たすため、取り出しアルゴリズムを次のようにする
 * 1.重みデータベースからある個数の要素グループを取り出す
 * 2.その中から最も参照回数の小さいものを選択する
 * 3.グループの要素数は重みランキングの上位ほど小さく、下位ほど大きくする
 * 付則として、参照回数がn回以上で重みが負のものは選択されないことにする
 * 調整パラメータは一巡の画像数と、付則の非選択となる参照回数となる
 * 
 * 取り出しは上記のノーマルモードと、新規参入画像(すなわち重みが0)の画像だけを取り出すゼロモードを設ける。
 * デフォルトのモードはゼロモード。重み0の画像が存在しない場合は自動的にノーマルモードに遷移する。
 */
public class SessionContext {
	private int sessionNo; // セッション番号
	private String dbFile;
	private WtContainer wc;
	private boolean extraMode = false;
	private boolean zeroMode = true;
	private int numDone = 0; // 鑑賞した画像の枚数
	private List<ViewElement> extraRing = new LinkedList<ViewElement>();
	private int extraIndex; // 次にextraRingから取り出されるエレメントのインデックス
	private List<ViewElement> doneList = new LinkedList<ViewElement>(); // 表示済みリスト
	private ViewElement ve;
	private ViewElement nextVe;
	private SimpleView sv;
	private Frame window;
	private int n = 0; // 一巡カウンタ
	private final double d = 1.1; // 重み勾配の次数
    private Point p;
	/**
	 * @param name 重みデータベースのファイル名
	 * @param sv 画像を表示するSimpleView
	 */
	SessionContext(int sessionNo, String dbFile, WtContainer wc, SimpleView sv,
			Frame window) {
		this.sessionNo = sessionNo;
		this.dbFile = dbFile;
		this.wc = wc;
		this.sv = sv;
		this.window = window;

		ve = getNextVe();
		nextVe = getNextVe();
	}
	
	/**
	 * 計時を中断する
	 */
	void pause() {
		ve.stopDspTime();
	}
	
	/**
	 * 計時を再開する
	 */
	void breakPause() {
		ve.startDspTime();
	}
	
	/**
	 * セッションを開始(再開)する
	 */
	void start() {
		if (numDone == 0) numDone++; // このコンテキストが初めて表示される時だけカウントする
		setTitle(ve);
		ve.startDspTime();
		sv.showVe(ve);
        if (p != null) sv.setPosition(p);
	}

	/**
	 * エキストラモードをトグルする
	 */
	void toggleExtraMode() {
		nextVe.flush();
		if (extraMode) {
			ve = getNextVe();
			nextVe = getNextVe();
		} else {
			if (extraRing.size() == 0) return;
			stopNormalDisp(ve);
			ve = getNextExtra();
			nextVe = getNextExtra();
		}
		extraMode = !extraMode;
		setTitle(ve);
		sv.showVe(ve);
	}

	/**
	 * セッションを停止する
	 */
	void stop() {
        p = sv.getPosition();
		ve.stopDspTime();
	}

	/**
	 * ノーマル表示時のViewElementの表示終了処理
	 */
	private void stopNormalDisp(ViewElement ve) {
		ve.flush();
		ve.changeVe();
		recalcWt(ve);
	}
	
	/**
	 * タイトルをセットする
	 */
	void setTitle(ViewElement ve) {
		String ex = extraMode ? " extra " : " ";
		window.setTitle("session " + (sessionNo + 1) + ex + ve.name +" "+
				ve.wt +" "+ ve.refs +" " + (ve.wt / ((ve.refs == 0) ? 1 : ve.refs) )
				+" "+ (int)(ve.mag * 100) +"%");
	}

	/**
	 * 次の画像を表示する
	 */
	void next() {
		if (extraMode) {
			ve.flush();
			ve = nextVe;
			nextVe = getNextExtra();
			sv.prepareImage(nextVe);
			setTitle(ve);
			sv.showVe(ve);
			return;
		}

		stopNormalDisp(ve);
		numDone++;
		ve = nextVe;
		setTitle(ve);
		sv.showVe(ve);
		nextVe = getNextVe();
	}

	/**
	 * extraRingから次のエレメントを取り出す
	 */
	private ViewElement getNextExtra() {
		ViewElement r = extraRing.get(extraIndex);
		extraIndex++;
		if (extraIndex >= extraRing.size()) extraIndex = 0;
		return r;
	}

	/**
	 * WtContainerから次のViewElementを取り出し、画像を準備する
	 * <p>
	 * 画像が準備できなかった場合、次のエレメントを取り出す。コンテナが空になったら、doneListを
	 * コンテナに入れ戻す。
	 */
	private ViewElement getNextVe() {
		int t = wc.getSize();
		int N = t / 16; // 重み付き画像ブラウズ一巡あたりの表示枚数
		if (N < 2) N = 2;
		ViewElement r = null;
		if (zeroMode) {
			r = wc.next0();
			if (r == null) {
				zeroMode = false;
				return getNextVe();
			}
			if (!sv.prepareImage(r)) {
				wc.decNum();
				return getNextVe();
			}
			doneList.add(r);
			System.out.println(r.name +" "+ r.wt +" "+ r.refs +" selected");
			return r;
		}
		
		if (n > N) {
			for (ViewElement d : doneList) {
				wc.add(d);
			}
			doneList.clear();
			n = 0;
		}
		// 今回表示する画像候補グループのサイズgを求める
		int g = Math.round(new Float(t / Math.pow(N, d+1) * (Math.pow(n+1, d+1) - Math.pow(n, d+1))));
		if (g == 0) g = 1;
		System.out.println("t, N, n, g= " + t +" "+ N +" "+ n +" "+ g);
		// 画像候補グループからタイムスタンプ最小のものを選び出す
		long m = Long.MAX_VALUE;
		for (int i=0; i < g; i++) {
			ViewElement temp = wc.next();
			if (temp == null) break;
			doneList.add(temp);
			if ((temp.wt < 0) && (temp.wt * temp.refs < -5000)) { // 重みが負の一定値を越えたら表示しない
				System.out.println(temp.name +" "+ temp.wt +" "+ temp.refs + " rejected");
				continue;
			}
			if (temp.ts < m) {
				r = temp;
				m = temp.ts;
			}
		}
		if (r == null) {
			n = N + 1;
			return getNextVe();
		}
		n++;
		System.out.println(r.name +" "+ r.wt +" "+ r.refs +" "+ (r.wt / ((r.refs == 0) ? 1 : r.refs)) +" selected");
		return r;
	}

	/**
	 * 画像がエキストラ指定された時の処理
	 */
	void extraSpecified() {
		if (ve.extra) return; // 既にエキストラ指定されていたらリターンする
		ve.extra = true;
		extraRing.add(ve);
	}

	/**
	 * 重みの再計算を実施し、計算に使用するパラメータをリセットする
	 * 
	 * @param ve 対象のViewElement
	 */
	private void recalcWt(ViewElement ve) {
		if (ve.laps < 300) return; // 表示時間が短すぎる場合は重みを再計算しない
		ve.refs++;
		int ud = ve.up - ve.down;
		int adjust = (int)((1 << Math.abs(ud)) * Math.signum(ud));
		long d = ve.laps + adjust * 1000 * ve.refs - 10000;
		ve.laps = 0;
		ve.up = 0;
		ve.down = 0;

		// 重みが非表示基準以下になった場合、処置を尋ねる
		int r = JOptionPane.YES_OPTION;
		if ((ve.wt + d) * ve.refs < -5000) {
			r = JOptionPane.showConfirmDialog(null, ve.name + " は次回以降非表示になります", "重みが基準値以下になりました",
						JOptionPane.YES_NO_OPTION);
		}
		// 上記ダイアログでNOだった場合、表示基準を満たすように重みを調整する
		if (r == JOptionPane.NO_OPTION) {
			ve.wt = -5000 / ve.refs;

		// 画像がエキストラ指定された場合、重みが今より小さくならないようにする
		} else if (ve.extra && ve.refs > 1 && d < (ve.wt / (ve.refs - 1))) {
			System.out.println("No selected.");
			ve.wt += ve.wt / (ve.refs - 1);
		} else ve.wt += d;
	}

	/**
	 * 終了処理。重みデータベースをファイルに出力する
	 * @return ゼロモードで鑑賞した画像の枚数
	 */
	int terminate() {
		if (!extraMode) stopNormalDisp(ve);
		
		for (ViewElement ve : doneList) {
			wc.add(ve);
		}
		wc.write(dbFile);
		return numDone;
	}

	private void saveScaledPic(ViewElement ve) {
		Runtime rt = Runtime.getRuntime();
		File f = new File(ve.name);
		String n = f.getName();
		String dest = f.getParent() + "/org/";

		if (new File(dest + n).exists()) {
			window.setTitle(ve.wt + " " + ve.refs + " " + ve.name
					+ " original exists.");
			return;
		}
		File d = new File(dest);
		if (!d.exists()) d.mkdirs();

		int h = ve.img.getHeight(sv);
		int s_h = (int) Math.round(h * ve.mag);
		if (s_h < 0) s_h = h;

		window.setTitle(ve.wt + " " + ve.refs + " " + ve.name
				+ " scaled pic saving ...");
		try {
			Process proc = rt.exec("cp " + ve.name + " " + dest + n);
			proc.waitFor();
			String command = "mogrify -scale x" + s_h + " " + ve.name;
			proc = rt.exec(command);
			proc.waitFor();
			System.out.println(command);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		window.setTitle(ve.wt + " " + ve.refs + " " + ve.name
				+ " saving done. scale=" + s_h);

		ve.mag = -1.0;

	}

	/**
	 * ゼロモードのトグルを実行する。エクストラモードだった場合は解除される。ゼロモードからはノーマルモードに遷移する
	 */
	void toggleZeroMode() {
		nextVe.flush();
		if (extraMode) {
			ve.flush();
			zeroMode = true;
		} else {
			stopNormalDisp(ve);
			zeroMode = !zeroMode;
		}
		extraMode = false;
		
		ve.refs++;
		ve = getNextVe();
		setTitle(ve);
		sv.showVe(ve);
		nextVe = getNextVe();
	}

}
