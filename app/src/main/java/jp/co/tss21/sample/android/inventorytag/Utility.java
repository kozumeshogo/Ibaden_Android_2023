package jp.co.tss21.sample.android.inventorytag;

public class Utility {

    /**
     * 16進数文字列をバイトに変換する
     * @param hex 16進数文字列
     * @return バイト配列
     */
    public static byte[] asByteArray(String hex) {
        // 文字列長の1/2の長さのバイト配列を生成
        byte[] bytes = new byte[(hex.length() + 1) / 2];

        // バイト配列の要素数分、処理を繰り返す
        for (int index = 0; index < bytes.length; index++) {
            // 16進数文字列をバイトに変換して配列に格納
            bytes[index] = (byte) Integer.parseInt(hex.substring(index * 2, (index + 1) * 2), 16);
        }

        return bytes;
    }
}
