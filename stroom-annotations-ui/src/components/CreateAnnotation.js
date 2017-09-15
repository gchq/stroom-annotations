import React from 'react';
import { connect } from 'react-redux'

import { createAnnotation } from '../actions/createAnnotation';

export const CreateAnnotation = ({annotationId, createAnnotation}) => (
    <div>
        <button onClick={() => createAnnotation(annotationId)}>Create Annotation</button>
    </div>
)

export default connect(
    (state) => ({
        annotationId: state.annotation.annotationId
    }),
    {createAnnotation}
)(CreateAnnotation)
