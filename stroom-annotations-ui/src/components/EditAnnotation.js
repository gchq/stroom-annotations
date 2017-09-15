import React from 'react';
import { connect } from 'react-redux'

import { updateAnnotation } from '../actions/updateAnnotation';
import { removeAnnotation } from '../actions/removeAnnotation';

import SelectStatus from './SelectStatus';

export const EditAnnotation = (props) => {

    const onContentChange = (e) => {
        const annotation = {
            content: e.target.value,
            status: props.annotation.status
        }

        props.updateAnnotation(props.annotation.id, annotation)
    }

    const onStatusChange = (e) => {
        const annotation = {
            content: props.annotation.content,
            status: e.target.value
        }

        props.updateAnnotation(props.annotation.id, annotation)
    }

    return (
        <div>
            <textarea value={props.annotation.content} onChange={onContentChange} />
            <SelectStatus value={props.annotation.status} onChange={onStatusChange} />

            <button onClick={() => props.removeAnnotation(props.annotation.id)}>Remove Annotation</button>
        </div>
    )
}

export default connect(
  (state) => ({
     annotation: state.annotation.annotation
  }),
  {
     updateAnnotation,
     removeAnnotation
  }
)(EditAnnotation)
