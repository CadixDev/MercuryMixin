/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package obf;

public class OutInner {
    public static void main(String[] args) {
        Runnable runnable = new Runnable() {
            public void run() {
                System.out.println("OutInner$1");
            }
        };
        Runnable runnable2 = new Runnable() {
            public void run() {
                System.out.println("OutInner$2");
            }
        };
    }
}
