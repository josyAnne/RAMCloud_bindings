/* Copyright (c) 2014 Stanford University
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR(S) DISCLAIM ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL AUTHORS BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package edu.stanford.ramcloud.test;

import org.testng.*;

/**
 * Provides a simple way to track test runs.
 */
public class DotTestListener extends TestListenerAdapter {
    private int m_count = 0;
 
    @Override
    public void onTestFailure(ITestResult tr) {
        log("F");
    }
 
    @Override
    public void onTestSkipped(ITestResult tr) {
        log("S");
    }
 
    @Override
    public void onTestSuccess(ITestResult tr) {
        log(".");
    }
 
    private void log(String string) {
        System.out.print(string);
        System.out.flush();
        if (++m_count % 40 == 0) {
            System.out.println();
        }
    }
} 
