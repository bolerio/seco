/*
 * This file is part of the Scriba source distribution. This is free, open-source 
 * software. For full licensing information, please see the LicensingInformation file
 * at the root level of the distribution.
 *
 * Copyright (c) 2006-2007 Kobrix Software, Inc.
 */
package seco.notebook;

/**
 *
 */
//notebook
//   insertionPoint
//   cellGroupBox
//      cellGroup
//           inputCellBox
//              commonCell
//                    paragraph
//                       charContent
//              cellHandle
//           insertionPoint
//           outputCellBox 
//              commonCell
//                     paragraph
//                         charContent
//              cellHandle
//            insertionPoint
//      cellHandle
public enum ElementType 
{
    notebook,
    //those 4 are the most important ones
    cellGroupBox,
    inputCellBox,
    outputCellBox,
    insertionPoint,
    cellHandle,
    cellGroup,
    commonCell,
    paragraph,
    charContent,
    expandHandle,
    component,
    htmlCell,
    fakeParagraph
}
