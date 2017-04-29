/*
 * ������: 2007/05/29
 *
 * TODO �����������줿�ե�����Υƥ�ץ졼�Ȥ��ѹ�����ˤϼ��إ�����:
 * ������ɥ� - ���� - Java - �����ɡ��������� - �����ɡ��ƥ�ץ졼��
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
 * ���å�����������롣���å����ϰ�ĤνŤߥǡ����١����˴�Ť�������ɽ���������󥹤�������<p>
 * �Ťߥǡ����١�������β������Ф����르�ꥺ��Ȥ��ơ��ʲ����׷��Ω�Ƥ롣<p>
 * 1.�Ťߤ��礭�������ۤɼ��Ф���Ψ���⤯�ʤ�
 * 2.�Ťߤ�Ʊ���٤ʤ黲�Ȳ���ξ�������Τ�ͥ�褹��->������ɽ�����郎�Ǥ�Ť���Τ��ѹ�
 * 3.ɽ���ϽŤߤ��礭����Τ��鳫�Ϥ����缡�㤤��Τ˰ܤ롣��䤷����ޤ��Ťߤ��礭����Τ����Ф����
 * �������׷�����������ᡢ���Ф����르�ꥺ��򼡤Τ褦�ˤ���
 * 1.�Ťߥǡ����١������餢��Ŀ������ǥ��롼�פ���Ф�
 * 2.�����椫��Ǥ⻲�Ȳ���ξ�������Τ����򤹤�
 * 3.���롼�פ����ǿ��ϽŤߥ�󥭥󥰤ξ�̤ۤɾ����������̤ۤ��礭������
 * ��§�Ȥ��ơ����Ȳ����n��ʾ�ǽŤߤ���Τ�Τ����򤵤�ʤ����Ȥˤ���
 * Ĵ���ѥ�᡼���ϰ��β������ȡ���§��������Ȥʤ뻲�Ȳ���Ȥʤ�
 * 
 * ���Ф��Ͼ嵭�ΥΡ��ޥ�⡼�ɤȡ�������������(���ʤ���Ťߤ�0)�β�����������Ф�����⡼�ɤ��ߤ��롣
 * �ǥե���ȤΥ⡼�ɤϥ���⡼�ɡ��Ť�0�β�����¸�ߤ��ʤ����ϼ�ưŪ�˥Ρ��ޥ�⡼�ɤ����ܤ��롣
 */
public class SessionContext {
	private int sessionNo; // ���å�����ֹ�
	private String dbFile;
	private WtContainer wc;
	private boolean extraMode = false;
	private boolean zeroMode = true;
	private int numDone = 0; // �վޤ������������
	private List<ViewElement> extraRing = new LinkedList<ViewElement>();
	private int extraIndex; // ����extraRing������Ф���륨����ȤΥ���ǥå���
	private List<ViewElement> doneList = new LinkedList<ViewElement>(); // ɽ���Ѥߥꥹ��
	private ViewElement ve;
	private ViewElement nextVe;
	private SimpleView sv;
	private Frame window;
	private int n = 0; // ��䥫����
	private final double d = 1.1; // �Ť߸��ۤμ���
    private Point p;
	/**
	 * @param name �Ťߥǡ����١����Υե�����̾
	 * @param sv ������ɽ������SimpleView
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
	 * �׻������Ǥ���
	 */
	void pause() {
		ve.stopDspTime();
	}
	
	/**
	 * �׻���Ƴ�����
	 */
	void breakPause() {
		ve.startDspTime();
	}
	
	/**
	 * ���å����򳫻�(�Ƴ�)����
	 */
	void start() {
		if (numDone == 0) numDone++; // ���Υ���ƥ����Ȥ�����ɽ������������������Ȥ���
		setTitle(ve);
		ve.startDspTime();
		sv.showVe(ve);
        if (p != null) sv.setPosition(p);
	}

	/**
	 * �������ȥ�⡼�ɤ�ȥ��뤹��
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
	 * ���å�������ߤ���
	 */
	void stop() {
        p = sv.getPosition();
		ve.stopDspTime();
	}

	/**
	 * �Ρ��ޥ�ɽ������ViewElement��ɽ����λ����
	 */
	private void stopNormalDisp(ViewElement ve) {
		ve.flush();
		ve.changeVe();
		recalcWt(ve);
	}
	
	/**
	 * �����ȥ�򥻥åȤ���
	 */
	void setTitle(ViewElement ve) {
		String ex = extraMode ? " extra " : " ";
		window.setTitle("session " + (sessionNo + 1) + ex + ve.name +" "+
				ve.wt +" "+ ve.refs +" " + (ve.wt / ((ve.refs == 0) ? 1 : ve.refs) )
				+" "+ (int)(ve.mag * 100) +"%");
	}

	/**
	 * ���β�����ɽ������
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
	 * extraRing���鼡�Υ�����Ȥ���Ф�
	 */
	private ViewElement getNextExtra() {
		ViewElement r = extraRing.get(extraIndex);
		extraIndex++;
		if (extraIndex >= extraRing.size()) extraIndex = 0;
		return r;
	}

	/**
	 * WtContainer���鼡��ViewElement����Ф����������������
	 * <p>
	 * �����������Ǥ��ʤ��ä���硢���Υ�����Ȥ���Ф�������ƥʤ����ˤʤä��顢doneList��
	 * ����ƥʤ������᤹��
	 */
	private ViewElement getNextVe() {
		int t = wc.getSize();
		int N = t / 16; // �Ť��դ������֥饦����䤢�����ɽ�����
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
		// ����ɽ������������䥰�롼�פΥ�����g�����
		int g = Math.round(new Float(t / Math.pow(N, d+1) * (Math.pow(n+1, d+1) - Math.pow(n, d+1))));
		if (g == 0) g = 1;
		System.out.println("t, N, n, g= " + t +" "+ N +" "+ n +" "+ g);
		// �������䥰�롼�פ��饿���ॹ����׺Ǿ��Τ�Τ����ӽФ�
		long m = Long.MAX_VALUE;
		for (int i=0; i < g; i++) {
			ViewElement temp = wc.next();
			if (temp == null) break;
			doneList.add(temp);
			if ((temp.wt < 0) && (temp.wt * temp.refs < -5000)) { // �Ťߤ���ΰ����ͤ�ۤ�����ɽ�����ʤ�
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
	 * �������������ȥ���ꤵ�줿���ν���
	 */
	void extraSpecified() {
		if (ve.extra) return; // ���˥������ȥ���ꤵ��Ƥ�����꥿���󤹤�
		ve.extra = true;
		extraRing.add(ve);
	}

	/**
	 * �ŤߤκƷ׻���»ܤ����׻��˻��Ѥ���ѥ�᡼����ꥻ�åȤ���
	 * 
	 * @param ve �оݤ�ViewElement
	 */
	private void recalcWt(ViewElement ve) {
		if (ve.laps < 300) return; // ɽ�����֤�û��������ϽŤߤ�Ʒ׻����ʤ�
		ve.refs++;
		int ud = ve.up - ve.down;
		int adjust = (int)((1 << Math.abs(ud)) * Math.signum(ud));
		long d = ve.laps + adjust * 1000 * ve.refs - 10000;
		ve.laps = 0;
		ve.up = 0;
		ve.down = 0;

		// �Ťߤ���ɽ�����ʲ��ˤʤä���硢���֤�Ҥͤ�
		int r = JOptionPane.YES_OPTION;
		if ((ve.wt + d) * ve.refs < -5000) {
			r = JOptionPane.showConfirmDialog(null, ve.name + " �ϼ���ʹ���ɽ���ˤʤ�ޤ�", "�Ťߤ�����Ͱʲ��ˤʤ�ޤ���",
						JOptionPane.YES_NO_OPTION);
		}
		// �嵭����������NO���ä���硢ɽ�������������褦�˽Ťߤ�Ĵ������
		if (r == JOptionPane.NO_OPTION) {
			ve.wt = -5000 / ve.refs;

		// �������������ȥ���ꤵ�줿��硢�Ťߤ�����꾮�����ʤ�ʤ��褦�ˤ���
		} else if (ve.extra && ve.refs > 1 && d < (ve.wt / (ve.refs - 1))) {
			System.out.println("No selected.");
			ve.wt += ve.wt / (ve.refs - 1);
		} else ve.wt += d;
	}

	/**
	 * ��λ�������Ťߥǡ����١�����ե�����˽��Ϥ���
	 * @return ����⡼�ɤǴվޤ������������
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
	 * ����⡼�ɤΥȥ����¹Ԥ��롣�������ȥ�⡼�ɤ��ä����ϲ������롣����⡼�ɤ���ϥΡ��ޥ�⡼�ɤ����ܤ���
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
