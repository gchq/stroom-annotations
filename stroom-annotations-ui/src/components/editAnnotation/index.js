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
     annotation: state.singleAnnotation.annotation,
     allowNavigation: state.ui.allowNavigation
  }),
  {
     editAnnotation,
     updateAnnotation,
     removeAnnotation
  }
)(withRouter(EditAnnotation))
