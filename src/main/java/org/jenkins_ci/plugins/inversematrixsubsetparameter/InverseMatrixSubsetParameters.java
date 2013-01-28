/*
 * The MIT License
 *
 * Copyright (c) 2013, Chris Johnson
 * Romain Seguy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkins_ci.plugins.inversematrixsubsetparameter;

import hudson.Extension;
import hudson.matrix.MatrixBuild;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
import hudson.plugins.parameterizedtrigger.matrix.MatrixSubsetAction;
import java.io.IOException;
import org.kohsuke.stapler.DataBoundConstructor;
/**
 *
 * @author Chris Johnson
 */
public class InverseMatrixSubsetParameters  extends AbstractBuildParameters{

    private boolean fromProject = false;
    
    @DataBoundConstructor
    public InverseMatrixSubsetParameters(boolean fromProject) {
        this.fromProject = fromProject;
    }
  
    public boolean getFromProject() {
        return fromProject;
    }

    @Override
    public Action getAction(AbstractBuild<?, ?> build, TaskListener listener) throws IOException, InterruptedException, DontTriggerException {

        MatrixSubsetAction triggeredAction = null;

        // check to see if this is a Matrix build
        if (!(build instanceof MatrixBuild)) {
            listener.getLogger().println("InverseMatrixSubsetParameters: Build not a Matrixbuild skipping.");
            return null;
        }
        //get existing MatrixSubsetAction
        MatrixSubsetAction currentAction = build.getAction(MatrixSubsetAction.class);
        if(currentAction != null){
            listener.getLogger().println("InverseMatrixSubsetParameters: found current filter and making inverse");
            String inverseFilter = "!(" + currentAction.getFilter() + ")";
            triggeredAction =  new MatrixSubsetAction(inverseFilter);
        } else if (fromProject) {
            listener.getLogger().println("InverseMatrixSubsetParameters: getting filter from project");
            MatrixBuild b = (MatrixBuild) build;
            String currentFilter = b.getProject().getCombinationFilter();
            if(currentFilter == null) {
                // No combination filter avaliable do not pass any parameters
                listener.getLogger().println("InverseMatrixSubsetParameters: No combination filter found in projct configuration, no parameters created.");
            } else {
                String inverseFilter = "!(" + currentFilter + ")";
                triggeredAction = new MatrixSubsetAction(inverseFilter);
            }
        } else {
            listener.getLogger().println("InverseMatrixSubsetParameters: No filter found no parameters created.");
        }
        return triggeredAction;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<AbstractBuildParameters> {
        @Override
        public String getDisplayName() {
            return Messages.InverseMatrixSubsetParameters_DisplayName();
        }
    }
}
