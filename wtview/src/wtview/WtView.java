package wtview;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * 重みつき画像ビューワ
 * <p>
 * 画像を切り替え指示を受ける毎に次々に表示する。表示中のユーザーレスポンス(表示時間、ランキングアップ/ダウンリクエスト)
 * に従って、次回表示される確率が変動する。<p>
 * ver2.0での変更点
 * <ul>
 * <li>起動時に新規登録画像をスキャンして表示するようにする→やはりRegisterで処理すべき
 * <li>ファイル移動に対応させる
 * <li>マルチセッションへの対応
 * <li>1回のセッションでは表示は1回だけにする。その代わり、エキストラリングを作成する
 * <li>表示のランキング範囲指定
 * <li>評価ストラテジーの改良
 *  <ul>
 *  <li>表示時間と評価キー入力を総合してランキングさせる
 *  <li> 表示時間評価の指標として、現在の表示時間の移動平均との比較を用いる
 *  <li>キー入力として、ランクアップ/ダウン/おまかせを設ける
 *  <li>表示時間評価の割引/オミットを受け付ける。計時停止キーを付ける?
 *  </ul>
 * </ul>
 */
class WtView {
	static ShowStrategy ss;
	private static JFrame f;
	private static SimpleView sv;

	/**
	 * 重みデータベースを読み込み、セッションをスタートさせる。セッションの切り替えはAlt+数字で行う
	 * @param args 重みデータベースのファイル名の並び
	 */
	public static void main(String[] args) {
		f = new JFrame();
		sv = new SimpleView();
		f.getContentPane().add(sv);
		ss = new ShowStrategy(args, sv, f);

		f.setSize(1280, 800);
		f.setVisible(true);
		sv.requestFocusInWindow();
	}
	
	static void next() {
		ss.next();
	}
	/**
	 * セッションを切り替える
	 * @param sessionNo　セッション番号。値が負ならエキストラモードのトグル
	 */
	static void changeSession(int sessionNo) {
		ss.changeSession(sessionNo);
	}
	static void quit() {
		int n = ss.terminate();
		f.setVisible(false);
		JOptionPane.showMessageDialog(null, n + " files displayed");
		System.exit(0);
	}
}
