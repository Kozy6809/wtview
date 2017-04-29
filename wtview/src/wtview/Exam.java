package wtview;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * 重みデータベースの内容を出力する
 */
class Exam {
	/**
	 * @param args[0]:データベースファイル名
	 */
	public static void main(String[] args) {
		WtContainer wc = null;
		try {
			FileInputStream fis = new FileInputStream(args[0]);
			ObjectInputStream ois = new ObjectInputStream(fis);
			wc = (WtContainer) ois.readObject();
			ois.close();
		} catch (ClassNotFoundException e) {
			System.out.println(args[0] + " collapsed.");
		} catch (IOException e) {
			System.out.println(args[0] + " is not found");
		}

		List<ViewElement> sv = wc.getViewElements();
		for (ViewElement ve : sv) {
			System.out.println(ve.wt +"\t"+ ve.refs +"\t"+ ve.ts +"\t"+ 
					ve.mag +"\t"+ ve.name);
		}
	}
}
