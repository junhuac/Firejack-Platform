/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package net.firejack.platform.core.config.meta.diff;

import net.firejack.platform.core.config.meta.IPackageDescriptorElement;

import java.util.Arrays;


public class PackageDescriptorElementDiff
        <P extends IPackageDescriptorElement, T extends IPackageDescriptorElement>
        implements IPackageDescriptorElementDiff<P, T> {

    protected DifferenceType type;
    protected T upgradeTarget;
    protected P targetParent;
    protected Object[] arguments;

    /**
     * @param type
     * @param upgradeTarget
     * @param arguments
     */
    public PackageDescriptorElementDiff(DifferenceType type, T upgradeTarget, Object... arguments) {
        this.type = type;
        this.upgradeTarget = upgradeTarget;
        this.arguments = arguments;
    }

    /**
     * @param type
     * @param targetParent
     * @param upgradeTarget
     * @param arguments
     */
    public PackageDescriptorElementDiff(DifferenceType type, P targetParent, T upgradeTarget, Object... arguments) {
        this.type = type;
        this.targetParent = targetParent;
        this.upgradeTarget = upgradeTarget;
        this.arguments = arguments;
    }

    /**
     * @param oldElement
     * @param newElement
     */
    public PackageDescriptorElementDiff(T oldElement, T newElement) {
        this.type = DifferenceType.UPDATED;
        this.upgradeTarget = oldElement;
        this.arguments = new Object[1];
        this.arguments[0] = newElement;
    }

    /**
     * @param added
     * @param element
     * @param arguments
     */
    public PackageDescriptorElementDiff(boolean added, T element, Object... arguments) {
        this.type = added ? DifferenceType.ADDED : DifferenceType.REMOVED;
        this.upgradeTarget = element;
        this.arguments = arguments.length > 0 ? arguments : null;
    }

    @Override
    public T getDiffTarget() {
        return upgradeTarget;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    public P getTargetParent() {
        return targetParent;
    }

    public DifferenceType getType() {
        return type;
    }

    @Override
    public void setToADDEDState() {
        if (this.type == DifferenceType.UPDATED) {
            this.upgradeTarget = getNewElement();
        }
        this.type = DifferenceType.ADDED;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getNewElement() {
        return this.type != DifferenceType.UPDATED || getArguments() == null ||
                getArguments().length == 0 ? null : (T) getArguments()[0];
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " : " + type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PackageDescriptorElementDiff that = (PackageDescriptorElementDiff) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(arguments, that.arguments)) return false;
        if (targetParent != null ? !targetParent.equals(that.targetParent) : that.targetParent != null) return false;
        if (type != that.type) return false;
        if (upgradeTarget != null ? !upgradeTarget.equals(that.upgradeTarget) : that.upgradeTarget != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (upgradeTarget != null ? upgradeTarget.hashCode() : 0);
        result = 31 * result + (targetParent != null ? targetParent.hashCode() : 0);
        result = 31 * result + (arguments != null ? Arrays.hashCode(arguments) : 0);
        return result;
    }
}