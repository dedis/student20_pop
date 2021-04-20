package com.github.dedis.student20_pop.qrcode;

import com.github.dedis.student20_pop.utility.qrcode.QRCodeListener;

public interface QRCodeScanningViewModel extends QRCodeListener {

  int getScanDescription();
}
