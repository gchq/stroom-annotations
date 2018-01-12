import { connect } from 'react-redux'
import { withRouter } from 'react-router'

import {
  updateAnnotation,
  editAnnotation
} from '../../actions/updateAnnotation'

import { removeAnnotation } from '../../actions/removeAnnotation'

import EditAnnotation from './editAnnotation'

export default connect(
  (state) => ({
    isClean: state.singleAnnotation.isClean,
    annotation: state.singleAnnotation.annotation
  }),
  {
    editAnnotation,
    updateAnnotation,
    removeAnnotation
  }
)(withRouter(EditAnnotation))
