<#--

     Copyright 2012-2015 Gunnar Morling (http://www.gunnarmorling.de/)
     and/or other contributors as indicated by the @authors tag. See the
     copyright.txt file in the distribution for a full listing of all
     contributors.

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.

-->
if ( ${ext.targetBeanName}.${ext.targetWriteAccessorName}() != null ) {
    <#if ext.existingInstanceMapping>
        ${ext.targetBeanName}.${ext.targetWriteAccessorName}().clear();
    </#if>
    <#if (exceptionTypes?size == 0) >
        <@_assignmentLine/>
        <#else>
        try {
            <@_assignmentLine/>
        }
        <#list exceptionTypes as exceptionType>
        catch ( <@includeModel object=exceptionType/> e ) {
            throw new RuntimeException( e );
        }
        </#list>
    </#if>
}
<#macro _assignmentLine>
     <@includeModel object=ext.targetType/> ${localVarName} = <@_assignment/>;
    if ( ${localVarName} != null ) {
        ${ext.targetBeanName}.${ext.targetWriteAccessorName}().<#if ext.targetType.collectionType>addAll<#else>putAll</#if>( ${localVarName} );
    }
</#macro>
<#macro _assignment>
    <@includeModel object=assignment
               targetBeanName=ext.targetBeanName
               raw=ext.raw
               existingInstanceMapping=ext.existingInstanceMapping
               targetReadAccessorName=ext.targetReadAccessorName
               targetWriteAccessorName=ext.targetWriteAccessorName
               targetType=ext.targetType/>
</#macro>
