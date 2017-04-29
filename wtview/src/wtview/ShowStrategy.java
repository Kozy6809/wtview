package wtview;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

/**
 * ɽ�����٤���������ꤹ�롣�������ڤ��ؤ��ꥯ�����Ȥ��褿�ݤˡ����ߤβ�����ɽ�����֤���Ťߤ�Ʒ׻����롣
 */
class ShowStrategy {
	private List<SessionContext> scs = new ArrayList<SessionContext>();
	private SessionContext sc;

	/**
	 * @param name �Ťߥǡ����١����Υե�����̾
	 * @param sv ������ɽ������SimpleView
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
	 * ���å������ڤ��ؤ���
	 * 
	 * @param ���å�����ֹ档�ͤ�-1�ʤ饨�����ȥ�⡼�ɤΥȥ��롢-2�ʤ饼��⡼�ɤΥȥ���
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
	 * ���β�����ɽ������
	 */
	void next() {
		sc.next();
	}

	/**
	 * �������������ȥ���ꤵ�줿���ν���
	 */
	void extraSpecified() {
		sc.extraSpecified();
	}
	
	/**
	 * �׻������Ǥ���
	 */
	void pause() {
		sc.pause();
	}
	
	/**
	 * �׻���Ƴ�����
	 */
	void breakPause() {
		sc.breakPause();
	}

	/**
	 * ��λ�������Ťߥǡ����١�����ե�����˽��Ϥ���
	 */
	int terminate() {
		int sum = 0;
		for (SessionContext sc : scs) {
			sum += sc.terminate();
		}
		return sum;
	}
}
