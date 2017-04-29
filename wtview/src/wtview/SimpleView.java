package wtview;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * ���ꤵ�줿������ɽ�����롣���������ޥ�ɤ������äƳƼ�����Ԥ���<p>
 * ���Υ��饹�����ǽ�������륳�ޥ��<p>
 * �����γ���̾�<p>
 * �����Υ�󥭥󥰥��å�/������
 * <p>
 * ����¾�Υ��ޥ�ɤ�WtView���ꤲ��
 * <ul>
 * <li>������ɽ��
 * <li>��λ
 * <li>�����򥨥����ȥ����
 * <li>�׻�������/�Ƴ�
 * <li>���å�����ڤ��ؤ�
 * </ul>
 *
 * <ul>
 *
 */
class SimpleView extends JScrollPane implements Runnable {
	private volatile ViewElement ve; // ����ɽ�����ViewElement
	// ������������륨����ȤΥꥹ��
	private BlockingQueue<ViewElement> picQueue = new LinkedBlockingQueue<ViewElement>();
	
	private enum Mag {
		MAGNIFY, SHRINK, FIT_WIDTH, FIT_HEIGHT, X1, X2
	};

	private ImgPanel viewPanel = new ImgPanel();

	private Image img;

	SimpleView() {
		super(JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		setViewportView(viewPanel);
		KeyAdapter ka = new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				handleKey(e);
			}
		};
		addKeyListener(ka);
		Thread t = new Thread(this);
		//t.setPriority(Thread.MIN_PRIORITY);
		t.start();
		
	}

	/**
	 * ��������󥰲��������������ѥ���åɤǼ»ܤ���
	 */
	public void run() {
		for (;;) {
			ViewElement p = null;
			try {
				p = picQueue.take();
			} catch (InterruptedException e) {
				// TODO ��ư�������줿 catch �֥�å�
				e.printStackTrace();
			}
			if (p.img == null) continue;
			int w = p.img.getWidth(this);
			int h = p.img.getHeight(this);
			int s_w = (int) Math.round(w * p.mag);
			int s_h = (int) Math.round(h * p.mag);
			BufferedImage s = new BufferedImage(s_w, s_h, BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D g = s.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			AffineTransform at = AffineTransform.getScaleInstance(p.mag, p.mag);
			g.drawImage(p.img, at, null);
			p.simg = s;
			if (p == ve) drawImage(p.simg, p); // �������줿��������ɽ�����٤���Τξ��
		}
	}
	/**
	 * ���̤�ɽ������륵�������֤�
	 */
	private Dimension getExtentSize() {
		return getViewport().getExtentSize();
	}

	/**
	 * ���ꤵ�줿������ɽ������
	 * 
	 * @param ve
	 *            ɽ���оݤ�ViewElement
	 * @return ɽ�������������true
	 */
	boolean showVe(ViewElement ve) {
		if (ve == null) return false;
		if (this.ve != null) this.ve.changeVe();
		this.ve = ve;
		if (!prepareImage(ve)) return false;
		
		// ɽ�����٤���������󥰲������ޤ���������Ƥ��ʤ����ϥ꥿���󤹤롣
		// �ºݤ�ɽ����������λ�����ݤ˹Ԥ���
		if (ve.mag != 1.0 && ve.simg == null) return true;
		
		drawImage((ve.mag == 1.0) ? ve.img : ve.simg, ve);

		return true;
	}
	
	/**
	 * �����ν����򤹤�
	 * @param ve�������оݤ�ViewElement
	 * @return ���������������true
	 */
	boolean prepareImage(ViewElement ve) {
		if (ve.img == null) {
			try {
				BufferedImage bi = ImageIO.read(new File(ve.name));
				ve.img = bi;
			} catch (IOException e) {
				System.out.println("can't read " + ve.name);
				return false;
			}
		}
		
		if (ve.mag != 1.0 && ve.simg == null) try {
			picQueue.put(ve);
		} catch (InterruptedException e) {
			// TODO ��ư�������줿 catch �֥�å�
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * ���ꤵ�줿������ɽ�����롣
	 * @param img ɽ�����륤�᡼��
	 * @param timeVe �׻��оݤ�viewElement��
	 */
	private void drawImage(Image img, final ViewElement timeVe) {
		this.img = img;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				timeVe.startDspTime();
				setViewportView(viewPanel);
			}
		});
	}

	/**
	 * ��Ψ�����ɤ˴�Ť���������󥰤��줿������ɽ������
	 * 
	 * @param mag ��Ψ������
	 */
	private void showScaledImage(Mag mag) {
		Dimension d = getExtentSize();
		int w = ve.img.getWidth(this);
		int h = ve.img.getHeight(this);

		switch (mag) {
		case X1: // ����
			ve.mag = 1.0;
			drawImage(ve.img, ve);
			return;
		case MAGNIFY: // 10%����
			ve.mag *= 1.1;
			break;
		case SHRINK: // 10%�̾�
			ve.mag *= 0.9;
			break;
		case X2: // 2�ܤ˳���
			ve.mag *= 2.0;
			break;
		case FIT_WIDTH: // ����ե��å�
			ve.mag = (double) d.width / (double) w;
			break;
		case FIT_HEIGHT: // �⤵��ե��å�
			ve.mag = (double) d.height / (double) h;
			break;
		}
		// ���ߤΥ�������󥰲�����õ��
		// ve.simg.flush();
		// ve.simg = null;
		try {
			picQueue.put(ve);
		} catch (InterruptedException e) {
			// TODO ��ư�������줿 catch �֥�å�
			e.printStackTrace();
		}
	}

	private void handleKey(KeyEvent e) {
		char c = e.getKeyChar();
		if (e.isAltDown()) {
			changeSession(c);
			return;
		}
		
		switch (c) {
		case ' ':
			WtView.next();
			break;
		case 'q':
			WtView.quit();
			break;
		case '1':
			showScaledImage(Mag.X1); // ����ɽ��
			break;
		case '2':
			showScaledImage(Mag.X2); // 2��ɽ��
			break;
		case 'w':
			showScaledImage(Mag.FIT_WIDTH); // ����ե��å�
			break;
		case 'h':
			showScaledImage(Mag.FIT_HEIGHT); // �⤵��ե��å�
			break;
		case '-':
			showScaledImage(Mag.SHRINK); // �̾�����
			break;
		case '^':
			showScaledImage(Mag.MAGNIFY); // ���礹��
			break;
		case 'u':
			ve.up++; // ��󥭥󥰥��åץꥯ������
			break;
		case 'd':
			ve.down++; // ��󥭥󥰥�����ꥯ������
			break;
		case 'x':
			WtView.ss.extraSpecified(); // �����򥨥����ȥ���ꤹ��
			break;
		case 's':
			//WtView.ss.writeScaledPic(); // ��������󥰤��줿��������¸����
			break;
		case 'p':
			WtView.ss.pause(); // �׻������Ǥ���
			break;
		case 'b':
			WtView.ss.breakPause(); // �׻���Ƴ�����
			break;
		default:
		}
	}

	/**
	 * ���å������ڤ��ؤ���Alt�����������줿���֤ǸƤӽФ����
	 * @param c �����פ��줿���ͥ�����e�����ʤ饨�����ȥ�⡼�ɤΥȥ��롣0�ʤ饼��⡼�ɤΥȥ���
 	 */
	private void changeSession(char c) {
		int i = 0;
		switch (c) {
		case '1': i=0; break;
		case '2': i=1; break;
		case '3': i=2; break;
		case '4': i=3; break;
		case '5': i=4; break;
		case '6': i=5; break;
		case '7': i=6; break;
		case '8': i=7; break;
		case '9': i=8; break;
		case 'z': i=0; break;
		case 'x': i=1; break;
		case 'c': i=2; break;
		case 'v': i=3; break;
		case 'b': i=4; break;
		case 'n': i=5; break;
		case 'm': i=6; break;
		case ',': i=7; break;
		case '.': i=8; break;
		case 'e': i=-1; break;
		case '0': i=-2; break;
		default: return;
		}
		WtView.changeSession(i);
	}
	
	private class ImgPanel extends JPanel {
		private Dimension d = new Dimension();

		ImgPanel() {
			super(new BorderLayout());
		}

		public Dimension getPreferredSize() {
			if (img == null)
				return getExtentSize();
			d.width = img.getWidth(this);
			d.height = img.getHeight(this);
			return d;
		}

		public void paint(Graphics g) {
			if (img == null)
				return;
			super.paint(g);
			int w = img.getWidth(this);
			int h = img.getHeight(this);
			Dimension d = getExtentSize();
			int x = (d.width > w) ? (d.width - w) / 2 : 0;
			int y = (d.height > h) ? (d.height - h) / 2 : 0;
			g.drawImage(img, x, y, this);
		}
	}

    Point getPosition() {
        return getViewport().getViewPosition();
    }
    
    void setPosition(final Point p) {
        EventQueue.invokeLater(new Runnable() {
			public void run() {
                getViewport().setViewPosition(p);
			}
		});
    }
}
