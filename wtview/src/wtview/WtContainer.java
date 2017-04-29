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
 * ViewElement�νŤߤĤ�����ƥʡ�������ȤϤ��νŤߤ����֤��졢Ʊ���ŤߤΥ�����Ȥϻ��Ȳ���ν��
 * ���֤���롣
 */
class WtContainer implements java.io.Serializable {
	/**
	 * <code>serialVersionUID</code> �Υ�����
	 */
	private static final long serialVersionUID = 1L;
	
	private SortedMap<Integer, List<ViewElement>> ct =
		new TreeMap<Integer, List<ViewElement>>();
	
	private List<ViewElement> ct0 = new LinkedList<ViewElement>(); // �Ť�0�β����Υꥹ��
	
	private int num; // �����ǤθĿ���

	/**
	 * ViewElement���ɲä���
	 * @param ve �ɲä��륨�����
	 */
	void add(ViewElement ve) {
		if (ve.refs == 0) {
			ct0.add(ve);
			return;
		}
		// �Ťߤ�ve.wt�ǤϤʤ���ve.wt/ve.refs���ѹ�
		int wt = ve.wt / ve.refs;
		List<ViewElement> l = ct.get(wt);
		if (l == null) {
			l = new LinkedList<ViewElement>();
			ct.put(wt, l);
		}
		l.add(ve);
	}

	/**
	 * �Ǥ�Ťߤ��礭��������Ȥ���Ф���
	 * ���Ф��줿������Ȥϥ���ƥʤ��������
	 * @return�����򤵤줿������ȡ�����ƥʤ˥�����Ȥ��ĤäƤ��ʤ�����null
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
	 * �Ť�0�Υ�����Ȥ���Ф�
	 * ���Ф��줿������Ȥϥ���ƥʤ��������
	 * @return�����򤵤줿������ȡ�����ƥʤ˽Ť�0�Υ�����Ȥ��ĤäƤ��ʤ�����null
	 */
	ViewElement next0() {
		if (ct0.size() == 0) return null;
		return ct0.remove(0);
	}

	/**
	 * ���ꤵ�줿�ե�����˥���ƥʤ����Ƥ�񤭽Ф�
	 * 
	 * @param �ե�����̾
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
	 * ���ꤵ�줿�ե����뤫�饳��ƥʤ����Ƥ��ɤ߹���
	 * 
	 * @param �ե�����̾
	 * @return �������줿����ƥʡ��ɤ߹��ߤ˼��Ԥ�������null
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
	 * �Ťߤ�0�Ǥʤ����ǤθĿ����֤���
	 */
	int getSize() {
		return num - ct0.size();
	}

	/**
	 * ɽ����ǽ�β��������ä���硢����ʬ���ǤθĿ��򸺤餹
	 */
	void decNum() {
		num--;
	}
	
	/**
	 * �����ǤθĿ����֤�
	 */
	int getNum() {
		return num;
	}
	/**
	 * �����ǤθĿ��򥻥åȤ���
	 */
	void setNum(int n) {
		num = n;
	}
	
	/**
	 * ��Ͽ����Ƥ��������ǤΥꥹ�Ȥ��֤�
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
	 * ��Ͽ����Ƥ�������ե�����̾�Υ��åȤ��֤�
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
