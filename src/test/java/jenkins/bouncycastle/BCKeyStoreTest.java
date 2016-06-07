/*
 Copyright 2014 Microsoft Open Technologies, Inc.
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package jenkins.bouncycastle;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.microsoftopentechnologies.azure.ServiceDelegateHelper;

public class BCKeyStoreTest {

    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test
    public void testObtainBCKeyStore() throws Exception {
        Method method = ServiceDelegateHelper.class.getDeclaredMethod("getBCProviderKeyStore");
        method.setAccessible(true);
        assertNotNull(method.invoke(null));
    }

}
