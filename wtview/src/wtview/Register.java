package wtview;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

/**
 * �Ťߥǡ����١����˿����ʲ�������Ͽ����
 * <p>
 * ���ޥ�ɥ饤���1: �ǡ����١����ե�����̾ �������åȥǥ��쥯�ȥ�̾
 * �������åȥǥ��쥯�ȥ�������ƤΥե����뤬�ǡ����١�������Ͽ����롣������Ͽ�Ѥߤξ��ϥ����åפ���롣
 * ���ޥ�ɥ饤���2: �ǡ����١����ե�����̾ �ǡ����ե�����̾
 * �ǡ����ե�����˵��Ҥ��줿�ե������ǡ����١�������Ͽ���롣������Ͽ�Ѥߤξ��ϥ����åפ���롣
 * �ǡ����ե�����ν񼰤�Exam�ν��Ϥ���ե������Ʊ��
 */
class Register {
	private static int n;
	/**
	 * 
	 */
	public static void main(String[] args) {
		WtContainer wc;
		File f = new File(args[0]);
		if (f.canRead()) wc = WtContainer.read(args[0]);
		else wc = new WtContainer();
		n = wc.getNum();

		File t = new File(args[1]);
		if (t.isDirectory()) {
			processDir(t, wc);
		}else {
			processFile(t, wc);
		}
		wc.setNum(n);
		wc.write(args[0]);
	}

	private static void processDir(File dir, WtContainer wc) {
		Set<String> ss = wc.getRegisteredFiles();
		File f = dir;
		File[] l = f.listFiles();
		int i=0;
		for (File e: l) {
			if (e.isDirectory()) continue;
			String s = e.getAbsolutePath();
			if (ss.contains(s)) continue;
			ViewElement ve = new ViewElement();
			ve.name = s;
			wc.add(ve);
			n++;
			System.out.println("registered " + s);
			i++;
		}
		System.out.println(i + " new files registered");
	}
	
	static void processFile(File f, WtContainer wc) {
		Set<String> ss = wc.getRegisteredFiles();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		int i=0;
		for (;;) {
			String s;
			try {
				s = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			if (s == null) break;
			String[] sa = s.split("\t");
			s = sa[4];
			if (ss.contains(sa[4])) continue;
			ViewElement ve = new ViewElement();
			ve.wt = Integer.parseInt(sa[0]);
			ve.refs = Integer.parseInt(sa[1]);
			ve.ts = Long.parseLong(sa[2]);
			ve.mag = Float.parseFloat(sa[3]);
			ve.name = sa[4];
			wc.add(ve);
			n++;
			//System.out.println("registered " + sa[4]);
			i++;
		}
		System.out.println(i + " files registered");
		wc.setNum(i);
		try {
			br.close();
		} catch (IOException e) {
			// TODO ��ư�������줿 catch �֥�å�
			e.printStackTrace();
		}
		
	}
}
