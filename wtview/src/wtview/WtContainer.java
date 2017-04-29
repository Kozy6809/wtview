package wtview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * ViewElementの重みつきコンテナ。エレメントはその重みで配置され、同じ重みのエレメントは参照回数の順に
 * 配置される。
 */
class WtContainer implements java.io.Serializable {
	/**
	 * <code>serialVersionUID</code> のコメント
	 */
	private static final long serialVersionUID = 1L;
	
	private SortedMap<Integer, List<ViewElement>> ct =
		new TreeMap<Integer, List<ViewElement>>();
	
	private List<ViewElement> ct0 = new LinkedList<ViewElement>(); // 重み0の画像のリスト
	
	private int num; // 全要素の個数。

	/**
	 * ViewElementを追加する
	 * @param ve 追加するエレメント
	 */
	void add(ViewElement ve) {
		if (ve.refs == 0) {
			ct0.add(ve);
			return;
		}
		// 重みをve.wtではなく、ve.wt/ve.refsに変更
		int wt = ve.wt / ve.refs;
		List<ViewElement> l = ct.get(wt);
		if (l == null) {
			l = new LinkedList<ViewElement>();
			ct.put(wt, l);
		}
		l.add(ve);
	}

	/**
	 * 最も重みが大きいエレメントを取り出す。
	 * 取り出されたエレメントはコンテナから除去される
	 * @return　選択されたエレメント。コンテナにエレメントが残っていない場合はnull
	 */
	ViewElement next() {
		if (ct.size() == 0) return null;
		int ctKey = ct.lastKey();
		List<ViewElement> l = ct.get(ctKey);
		ViewElement ve = l.remove(0);
		if (l.size() == 0) ct.remove(ctKey);
		return ve;
	}

	/**
	 * 重み0のエレメントを取り出す
	 * 取り出されたエレメントはコンテナから除去される
	 * @return　選択されたエレメント。コンテナに重み0のエレメントが残っていない場合はnull
	 */
	ViewElement next0() {
		if (ct0.size() == 0) return null;
		return ct0.remove(0);
	}

	/**
	 * 指定されたファイルにコンテナの内容を書き出す
	 * 
	 * @param ファイル名
	 */
	void write(String name) {
		try {
			PrintStream ps = new PrintStream(name);
			List<ViewElement> sv = getViewElements();
			for (ViewElement ve : sv) {
				ps.println(ve.wt +"\t"+ ve.refs +"\t"+ ve.ts +"\t"+ 
						ve.mag +"\t"+ ve.name);
			}

		} catch (FileNotFoundException e) {
			System.out.println(name + " not exist !!");
		}
		/**
		try {
			FileOutputStream fos = new FileOutputStream(name);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}

	/**
	 * 指定されたファイルからコンテナの内容を読み込む
	 * 
	 * @param ファイル名
	 * @return 生成されたコンテナ。読み込みに失敗した場合はnull
	 */
	static WtContainer read(String name) {
		WtContainer wc = new WtContainer();
		File f = new File(name);
		Register.processFile(f, wc);
		return wc;
		/**
		WtContainer wc = null;
		try {
			FileInputStream fis = new FileInputStream(name);
			ObjectInputStream ois = new ObjectInputStream(fis);
			wc = (WtContainer) ois.readObject();
			ois.close();
		} catch (ClassNotFoundException e) {
			System.out.println(name + " collapsed.");
			return null;
		} catch (IOException e) {
			System.out.println(name + " is not found");
			return null;
		}
	
		
		return wc;
		*/
	}
	
	/**
	 * 重みが0でない要素の個数を返す。
	 */
	int getSize() {
		return num - ct0.size();
	}

	/**
	 * 表示不能の画像があった場合、その分要素の個数を減らす
	 */
	void decNum() {
		num--;
	}
	
	/**
	 * 全要素の個数を返す
	 */
	int getNum() {
		return num;
	}
	/**
	 * 全要素の個数をセットする
	 */
	void setNum(int n) {
		num = n;
	}
	
	/**
	 * 登録されている全要素のリストを返す
	 */
	List<ViewElement> getViewElements() {
		List<ViewElement> r = new LinkedList<ViewElement>();
		r.addAll(ct0);
		Collection<List<ViewElement>> cl = ct.values();
		for (List<ViewElement> l: cl) {
			r.addAll(l);
		}
		return r;
	}

	/**
	 * 登録されている画像ファイル名のセットを返す
	 */
	Set<String> getRegisteredFiles() {
		Set<String> r = new HashSet<String>();
		List<ViewElement> lv = getViewElements();
		for (ViewElement ve: lv) {
			r.add(ve.name);
		}
		return r;
	}
}
